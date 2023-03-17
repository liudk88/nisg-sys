package com.hcxinan.sys.sso;
/**
 * 用于登陆的接口
 * @Author liudk by 2022/3/24 上午11:08
 */
public interface ISubject<T extends IToken> {
    void login(T token);
}
