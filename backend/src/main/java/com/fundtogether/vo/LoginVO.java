package com.fundtogether.vo;

import lombok.Data;

/**
 * 登录视图对象
 * <p>
 * 用于在用户登录成功后返回给前端的响应数据，
 * 包含JWT令牌和用户的基本信息，前端可据此维持登录状态和展示用户信息。
 * </p>
 */
@Data
public class LoginVO {
    /** JWT令牌，前端需在后续请求的Authorization头中携带此令牌 */
    private String token;
    /** 用户ID */
    private Long id;
    /** 用户账号名 */
    private String account;
    /** 用户昵称 */
    private String nickname;
    /** 用户头像URL */
    private String avatar;
    /** 用户角色编码 */
    private Integer role;
}
