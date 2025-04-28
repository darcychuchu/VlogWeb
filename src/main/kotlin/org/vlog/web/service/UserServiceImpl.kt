package org.vlog.web.service

import org.vlog.web.dto.ApiResponse
import org.vlog.web.dto.UsersDto
import org.vlog.web.entity.Users
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import org.springframework.web.multipart.MultipartFile
import java.util.*

@Service
class UserServiceImpl(
    private val restTemplate: RestTemplate,
    @Value("\${api.base-url}") private val apiBaseUrl: String
) : UserService {
    ///private val logger = LoggerFactory.getLogger(UserServiceImpl::class.java)
    
    // 用户API的基础URL，不包含videos前缀
    private val userApiBaseUrl = apiBaseUrl.replace("/videos", "")

    override fun checkUsernameExists(username: String): Boolean {
        try {
            ///logger.info("Checking if username exists: $username")
            
            val responseType = object : ParameterizedTypeReference<ApiResponse<Boolean>>() {}
            val response = restTemplate.exchange<ApiResponse<Boolean>>(
                "$userApiBaseUrl/users/stated-name?username=$username",
                HttpMethod.GET,
                HttpEntity.EMPTY,
                responseType
            )
            
            val apiResponse = response.body
            if (apiResponse != null) {
                //logger.info("API response received: code=${apiResponse.code}, message=${apiResponse.message}, data=${apiResponse.data}")
                // API返回的data为true表示用户名已存在，为false表示用户名不存在
                val exists = apiResponse.data == true
                ///logger.info("Username $username exists: $exists")
                return exists
            }
            
            ///logger.warn("API response body is null for username check: $username")
            return false
        } catch (e: RestClientException) {
            ////logger.error("Error checking username existence: ${e.message}", e)
            return false
        }
    }

    override fun checkNicknameExists(nickname: String): Boolean {
        try {
            ///logger.info("Checking if nickname exists: $nickname")
            
            val responseType = object : ParameterizedTypeReference<ApiResponse<Boolean>>() {}
            val response = restTemplate.exchange<ApiResponse<Boolean>>(
                "$userApiBaseUrl/users/stated-nickname?nickname=$nickname",
                HttpMethod.GET,
                HttpEntity.EMPTY,
                responseType
            )
            
            val apiResponse = response.body
            if (apiResponse != null) {
                ///logger.info("API response received: code=${apiResponse.code}, message=${apiResponse.message}, data=${apiResponse.data}")
                // API返回的data为true表示昵称已存在，为false表示昵称不存在
                val exists = apiResponse.data == true
                ///logger.info("Nickname $nickname exists: $exists")
                return exists
            }
            
            ///logger.warn("API response body is null for nickname check: $nickname")
            return false
        } catch (e: RestClientException) {
            ///logger.error("Error checking nickname existence: ${e.message}", e)
            return false
        }
    }

    override fun login(username: String, password: String, androidId: String?): UsersDto? {
        try {
            ///logger.info("Attempting login for user: $username")
            
            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
            
            val map = LinkedMultiValueMap<String, String>()
            map.add("username", username)
            map.add("password", password)
            if (!androidId.isNullOrEmpty()) {
                map.add("android_id", androidId)
            }
            
            val request = HttpEntity(map, headers)
            
            val responseType = object : ParameterizedTypeReference<ApiResponse<UsersDto>>() {}
            val response = restTemplate.exchange<ApiResponse<UsersDto>>(
                "$userApiBaseUrl/users/login",
                HttpMethod.POST,
                request,
                responseType
            )
            
            val apiResponse = response.body
            if (apiResponse != null) {
                ///logger.info("API response received: code=${apiResponse.code}, message=${apiResponse.message}, data=${apiResponse.data}")
                if (apiResponse.data != null) {
                    ////logger.info("Login successful for user: $username")
                    return apiResponse.data
                }
            }
            
            return null
        } catch (e: RestClientException) {
            ///logger.error("Error during login: ${e.message}", e)
            return null
        }
    }

    override fun register(users: Users, username: String, password: String, nickname: String): UsersDto? {
        try {
            ///logger.info("Attempting registration for user: $username with nickname: $nickname")
            
            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
            
            val map = LinkedMultiValueMap<String, String>()
            map.add("username", username)
            map.add("password", password)
            map.add("nickname", nickname)
            
            // 添加可选字段
            if (!users.description.isNullOrEmpty()) {
                map.add("description", users.description!!)
            }
            
            val request = HttpEntity(map, headers)
            
            val responseType = object : ParameterizedTypeReference<ApiResponse<UsersDto>>() {}
            val response = restTemplate.exchange<ApiResponse<UsersDto>>(
                "$userApiBaseUrl/users/register",
                HttpMethod.POST,
                request,
                responseType
            )
            
            val apiResponse = response.body
            if (apiResponse != null) {
                ///logger.info("API response received: code=${apiResponse.code}, message=${apiResponse.message}, data=${apiResponse.data}")
                if (apiResponse.data != null) {
                   //// logger.info("Registration successful for user: $username")
                    return apiResponse.data
                }
            }
            
            return null
        } catch (e: RestClientException) {
            ///logger.error("Error during registration: ${e.message}", e)
            return null
        }
    }

    override fun updateUserInfo(name: String, token: String, nickname: String?, avatarFile: MultipartFile?): UsersDto? {
        try {
            ///logger.info("Updating user info for: $name")
            
            val headers = HttpHeaders()
            headers.contentType = MediaType.MULTIPART_FORM_DATA
            
            val map = LinkedMultiValueMap<String, Any>()
            if (!nickname.isNullOrEmpty()) {
                map.add("nickname", nickname)
            }
            
            if (avatarFile != null && !avatarFile.isEmpty) {
                // 创建资源对象
                val resource = avatarFile.resource
                map.add("avatar_file", resource)
            }
            
            val request = HttpEntity(map, headers)
            
            val responseType = object : ParameterizedTypeReference<ApiResponse<UsersDto>>() {}
            val response = restTemplate.exchange<ApiResponse<UsersDto>>(
                "$userApiBaseUrl/users/updated/$name/$token",
                HttpMethod.POST,
                request,
                responseType
            )
            
            val apiResponse = response.body
            if (apiResponse != null) {
                ///logger.info("API response received: code=${apiResponse.code}, message=${apiResponse.message}, data=${apiResponse.data}")
                if (apiResponse.data != null) {
                    ///logger.info("User info update successful for: $name")
                    return apiResponse.data
                }
            }
            
            return null
        } catch (e: RestClientException) {
            ////logger.error("Error updating user info: ${e.message}", e)
            return null
        }
    }
}
