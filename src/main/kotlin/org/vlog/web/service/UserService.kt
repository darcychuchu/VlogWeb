package org.vlog.web.service

import org.vlog.web.dto.UsersDto
import org.vlog.web.entity.Users
import org.springframework.web.multipart.MultipartFile

interface UserService {
    /**
     * 检查用户名是否已存在
     * @param username 用户名
     * @return true表示已存在，false表示不存在
     */
    fun checkUsernameExists(username: String): Boolean
    
    /**
     * 检查昵称是否已存在
     * @param nickname 昵称
     * @return true表示已存在，false表示不存在
     */
    fun checkNicknameExists(nickname: String): Boolean
    
    /**
     * 用户登录
     * @param username 用户名
     * @param password 密码
     * @param androidId 安卓设备ID（可选）
     * @return 登录成功返回用户信息，失败返回null
     */
    fun login(username: String, password: String, androidId: String?): UsersDto?
    
    /**
     * 用户注册
     * @param users 用户信息
     * @param username 用户名
     * @param password 密码
     * @param nickname 昵称
     * @return 注册成功返回用户信息，失败返回null
     */
    fun register(users: Users, username: String, password: String, nickname: String): UsersDto?
    
    /**
     * 更新用户信息
     * @param name 用户名
     * @param token 访问令牌
     * @param nickname 新昵称（可选）
     * @param avatarFile 新头像文件（可选）
     * @return 更新成功返回用户信息，失败返回null
     */
    fun updateUserInfo(name: String, token: String, nickname: String?, avatarFile: MultipartFile?): UsersDto?
}
