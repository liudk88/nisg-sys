package com.hcxinan.sys.controller;

import com.alibaba.fastjson.JSON;
import com.hcxinan.core.inte.system.IAttachment;
import com.hcxinan.core.inte.system.ICacheAttachmentManager;
import com.hcxinan.core.inte.system.IUser;
import com.hcxinan.core.inte.system.IUserService;
import com.hcxinan.core.util.JsonResult;
import com.hcxinan.sys.attachment.AttachmentManager;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author liudk
 * @Description: 管理附件的控制器
 * @date 21-9-10 下午5:55
 */
@RestController
@RequestMapping("/attachment")
public class AttachmentController {
    private static final Logger logger = LoggerFactory.getLogger(AttachmentController.class);

    @Autowired(required = false)
    private ICacheAttachmentManager cacheAttachmentManager;
    @Autowired
    private IUserService userService;

    @GetMapping
    public JsonResult queryList(@RequestParam("fileToken") String fileToken,String clearCache){
        Map<String,Object> result=new HashMap<>();
        try {
            if("1".equals(clearCache)){
                /*
                 * 清楚缓存中的数据.
                 * 用户可能在编辑页面里增加或删除了附件，而后面没有单击保存，所以也没有对操作进行持久化，
                 * 这时缓存的数据和数据库就不一致了，当用户重新查询附件的时候，至少说明用户已经离开过编辑页面了，
                 * 那么如果缓存中还有数据，说明就是在上次编辑后没有持久化文件数据，缓存中的数据就属于脏数据，需要删掉。
                 *
                 * 但在并发操作的情况下是不能这么做的，如一个用户在编辑过程中，另外一个用户打开了会把编辑用户的数据清掉
                 * */
                Runnable run=new Runnable() {
                    @Override
                    public void run() {
                        cacheAttachmentManager.clearCache(fileToken);
                    }
                };
                new Thread(run).start();
            }
            List<IAttachment> datas=cacheAttachmentManager.getAttachmentsByToken(fileToken);
            List resultDatas=datas;
            if(datas!=null && datas.size()>0){
                String[] userids=datas.stream().map(IAttachment::getUploader).toArray(String[]::new);
                if(userids!=null){
                    List<IUser> users=userService.getUsersById(userids);
                    Map<String,String> userMap=users.stream().collect(Collectors.toMap(IUser::getId,IUser::getName));
                    resultDatas=datas.stream().map(iAttachment -> {
                        Map map=JSON.parseObject(JSON.toJSONString(iAttachment),Map.class);
                        map.put("createUserName",userMap.get(iAttachment.getUploader()));
                        map.put("charset",getCharset(iAttachment.getInputStream()));
                        return map;
                    }).collect(Collectors.toList());
                }
            }
            result.put("datas",resultDatas);
            return JsonResult.success(result);
        }catch (Exception e){
            e.printStackTrace();
        }
        return JsonResult.error(result);
    }
    @RequestMapping("/upload")
    public JsonResult upload(@RequestParam(name="fileToken",required = false) String fileToken, MultipartFile file, HttpServletRequest request) throws IOException {
        String fileName=file.getOriginalFilename();
        String charset=request.getParameter("charset");
        if(StringUtils.isNotBlank(charset)){//有限制文件的编码格式
            String realCharset=getCharset(file.getInputStream());
            if(!charset.equals(realCharset)){
                return JsonResult.error("请上传"+charset+"编码格式的文件（当前上传的是"+realCharset+"编码格式文件）！");
            }
        }
        //把文件上传的缓存
        Map<String,Object> resultData=new HashMap<>();

        String[] ids=cacheAttachmentManager.upload(fileToken,new HashMap(){{
            put(fileName,file.getBytes());
        }});
        resultData.put("fileToken",ids[0]);
        resultData.put("aid",ids[1]);
        return JsonResult.success(resultData);
    }


    @GetMapping("/{attachmentId}/download")
    public ResponseEntity<InputStreamResource> download(@PathVariable("attachmentId")String attachmentId) throws IOException {
        IAttachment attachment = cacheAttachmentManager.getAttachmentById(attachmentId);
        if(attachment!=null){
            String path= AttachmentManager.hcuploads.getAbsolutePath() + File.separator + attachment.getAttachmentId();
            logger.info("下载文件所在目录："+path);
            FileSystemResource file = new FileSystemResource(path);
            if (file.exists()) {
                //设置响应头
                HttpHeaders headers = new HttpHeaders();
                headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
                headers.add("Content-Disposition", String.format("attachment; filename=\"%s\"", new String(attachment.getName().getBytes("utf-8"),"iso-8859-1")));
                headers.add("Pragma", "no-cache");
                headers.add("Expires", "0");

                return ResponseEntity.ok()
                        .headers(headers)
                        .contentLength(file.contentLength())
                        .contentType(MediaType.parseMediaType("application/octet-stream"))
                        .body(new InputStreamResource(file.getInputStream()));
            }
        }
        return null;
    }

    @DeleteMapping("/{attachmentId}")
    public JsonResult delete(@PathVariable("attachmentId") String attachmentId){
        cacheAttachmentManager.removeCacheFile(attachmentId);
        return JsonResult.success("success");
    }
    /**
     * @Author liudk by 2022/7/28 下午9:23
     * @description：把其他文件令牌更新合并到某个令牌
     *
     * @Param fileToken：需要最终合并成的令牌
     * @Param ids：需要被合并的令牌
     *
     * @Throws
     *
     * @Return
     */
    @GetMapping("/{fileToken}/batchUpdate/{ids}")
    public JsonResult updateFileToken(@PathVariable("fileToken") String fileToken,@PathVariable("ids") String... ids){
        cacheAttachmentManager.mergeFileToken(fileToken,ids);
        return JsonResult.success("success");
    }
    /**
     * @Author liudk by 2022/8/10 下午5:06
     * @description：多文件打包下载，下载zip的压缩包
     *
     * @Param 
     *
     * @Throws
     *
     * @Return 
     */
    @GetMapping("/download/{fileTokens}")
    public ResponseEntity<InputStreamResource> downloadPackZip(@RequestParam(name="zipFileName")String zipFileName,@PathVariable("fileTokens") String... fileTokens) throws IOException {
        List<IAttachment> attachments=cacheAttachmentManager.getAttachmentsByTokens(fileTokens);
        if(attachments!=null && !attachments.isEmpty()){
            String path= AttachmentManager.hcuploads.getAbsolutePath() + File.separator + zipFileName;
            File zipfile=new File(path);
            byte[] buf = new byte[1024];
            ZipOutputStream out = null;
            try {
                //设置输出流
                out = new ZipOutputStream(new FileOutputStream(zipfile));
                for(IAttachment attachment:attachments){
                    //读取相关的文件
                    FileInputStream in = attachment.getInputStream();
                    out.putNextEntry(new ZipEntry(attachment.getName()));
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        //写入输出流中
                        out.write(buf, 0, len);
                    }
                    out.closeEntry();
                    in.close();
                }
                //关闭流
                out.close();
                logger.info("压缩完成.");

                logger.info("下载压缩文件所在目录："+path);
                FileSystemResource file = new FileSystemResource(path);
                if (file.exists()) {
                    //设置响应头
                    HttpHeaders headers = new HttpHeaders();
                    headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
                    headers.add("Content-Disposition", String.format("attachment; filename=\"%s\"", new String(zipFileName.getBytes("utf-8"),"iso-8859-1")));
                    headers.add("Pragma", "no-cache");
                    headers.add("Expires", "0");

                    ResponseEntity responseEntity=ResponseEntity.ok()
                            .headers(headers)
                            .contentLength(file.contentLength())
                            .contentType(MediaType.parseMediaType("application/octet-stream"))
                            .body(new InputStreamResource(file.getInputStream()));
                    File zipf=new File(path);
                    zipf.delete();
                    return responseEntity;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private String getCharset(InputStream in) {
        String charset = "GBK";
        byte[] first3Bytes = new byte[3];
        try {
            boolean checked=false;
            BufferedInputStream bis = new BufferedInputStream( in );
            bis.mark( 0 );
            int read = bis.read( first3Bytes, 0, 3 );
            if ( read == -1 ) return charset;
            if ( first3Bytes[0] == (byte) 0xFF && first3Bytes[1] == (byte) 0xFE ) {
                charset = "UTF-16LE";
                checked = true;
            }
            else if ( first3Bytes[0] == (byte) 0xFE && first3Bytes[1] == (byte) 0xFF ) {
                charset = "UTF-16BE";
                checked = true;
            }
            else if ( first3Bytes[0] == (byte) 0xEF && first3Bytes[1] == (byte) 0xBB && first3Bytes[2] == (byte) 0xBF ) {
                charset = "UTF-8";
                checked = true;
            }
            bis.reset();
            if ( !checked ) {
                //    int len = 0;
                int loc = 0;
                while ( (read = bis.read()) != -1 ) {
                    loc++;
                    if ( read >= 0xF0 ) break;
                    if ( 0x80 <= read && read <= 0xBF ) // 单独出现BF以下的，也算是GBK
                        break;
                    if ( 0xC0 <= read && read <= 0xDF ) {
                        read = bis.read();
                        if ( 0x80 <= read && read <= 0xBF ) // 双字节 (0xC0 - 0xDF) (0x80
                            // - 0xBF),也可能在GB编码内
                            continue;
                        else break;
                    }
                    else if ( 0xE0 <= read && read <= 0xEF ) {// 也有可能出错，但是几率较小
                        read = bis.read();
                        if ( 0x80 <= read && read <= 0xBF ) {
                            read = bis.read();
                            if ( 0x80 <= read && read <= 0xBF ) {
                                charset = "UTF-8";
                                break;
                            }
                            else break;
                        }
                        else break;
                    }
                }
                //System.out.println( loc + " " + Integer.toHexString( read ) );
            }
            bis.close();
        } catch ( Exception e ) {
            e.printStackTrace();
        }

        return charset;
    }
}
