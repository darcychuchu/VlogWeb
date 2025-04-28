package org.vlog.web.controller

import org.vlog.web.dto.UsersDto
import org.vlog.web.entity.Users
import org.vlog.web.service.CaptchaService
import org.vlog.web.service.UserService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpSession
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
@RequestMapping("/user")
class UserController(
    private val userService: UserService,
    private val captchaService: CaptchaService
) {
    //private val logger = LoggerFactory.getLogger(UserController::class.java)

    // 登录页面
    @GetMapping("/login")
    fun loginPage(model: Model): String {
        //logger.info("Accessing login page")
        return "user/login"
    }

    // 注册页面
    @GetMapping("/register")
    fun registerPage(model: Model): String {
        //logger.info("Accessing register page")
        return "user/register"
    }

    // 个人资料页面
    @GetMapping("/profile")
    fun profilePage(model: Model, session: HttpSession): String {
        //logger.info("Accessing profile page")

        val user = session.getAttribute("user") as? UsersDto ?: return "redirect:/user/login"

        model.addAttribute("user", user)
        return "user/profile"
    }

    // 检查用户名是否存在及格式是否正确
    @GetMapping("/check-username")
    @ResponseBody
    fun checkUsername(@RequestParam("username") username: String): Map<String, Any> {
        //logger.info("Checking username: $username")

        // 验证用户名格式（只能是字母和数字的组合）
        val isValidFormat = username.matches(Regex("^[a-zA-Z0-9]+$"))
        if (!isValidFormat) {
            return mapOf(
                "exists" to false,
                "valid" to false,
                "message" to "用户名只能包含字母和数字"
            )
        }

        val exists = userService.checkUsernameExists(username)
        return mapOf(
            "exists" to exists,
            "valid" to true,
            "message" to if (exists) "用户名已存在" else "用户名可用"
        )
    }

    // 检查昵称是否存在
    @GetMapping("/check-nickname")
    @ResponseBody
    fun checkNickname(@RequestParam("nickname") nickname: String): Map<String, Any> {
        //logger.info("Checking nickname: $nickname")
        val exists = userService.checkNicknameExists(nickname)
        //logger.info("Nickname check result: $nickname exists=$exists")
        return mapOf(
            "exists" to exists,
            "message" to if (exists) "昵称已存在" else "昵称可用"
        )
    }

    // 处理登录请求
    @PostMapping("/login")
    fun login(
        @RequestParam("username") username: String,
        @RequestParam("password") password: String,
        model: Model,
        session: HttpSession,
        redirectAttributes: RedirectAttributes
    ): String {
        //logger.info("Processing login request for user: $username")

        if (username.isBlank() || password.isBlank()) {
            //logger.warn("Username or password is blank")
            redirectAttributes.addFlashAttribute("error", "用户名和密码不能为空")
            return "redirect:/user/login"
        }

        val user = userService.login(username, password, null)
        if (user == null) {
            //logger.warn("Login failed for user: $username")
            redirectAttributes.addFlashAttribute("error", "用户名或密码错误")
            return "redirect:/user/login"
        }

        // 登录成功，保存用户信息到会话
        //logger.info("Login successful for user: $username")
        session.setAttribute("user", user)
        redirectAttributes.addFlashAttribute("success", "登录成功")
        return "redirect:/"
    }

    // 处理注册请求
    @PostMapping("/register")
    fun register(
        @ModelAttribute users: Users,
        @RequestParam("username") username: String,
        @RequestParam("password") password: String,
        @RequestParam("nickname") nickname: String,
        @RequestParam("confirmPassword") confirmPassword: String,
        @RequestParam("captcha") captcha: String,
        @RequestParam("captchaSessionId") captchaSessionId: String,
        model: Model,
        session: HttpSession,
        redirectAttributes: RedirectAttributes
    ): String {
        //logger.info("Processing registration request for user: $username with nickname: $nickname")

        // 验证输入
        if (username.isBlank() || password.isBlank() || nickname.isBlank() || captcha.isBlank()) {
            //logger.warn("Username, password, nickname or captcha is blank")
            redirectAttributes.addFlashAttribute("error", "用户名、密码、昵称和验证码不能为空")
            return "redirect:/user/register"
        }

        // 验证用户名格式（只能是字母和数字的组合）
        if (!username.matches(Regex("^[a-zA-Z0-9]+$"))) {
            //logger.warn("Invalid username format: $username")
            redirectAttributes.addFlashAttribute("error", "用户名只能包含字母和数字")
            return "redirect:/user/register"
        }

        // 验证密码长度（至少6位）
        if (password.length < 6) {
            //logger.warn("Password too short")
            redirectAttributes.addFlashAttribute("error", "密码至少需要6位")
            return "redirect:/user/register"
        }

        if (password != confirmPassword) {
            //logger.warn("Passwords do not match")
            redirectAttributes.addFlashAttribute("error", "两次输入的密码不一致")
            return "redirect:/user/register"
        }

        // 验证验证码
        if (!captchaService.validateCaptcha(captchaSessionId, captcha)) {
            //logger.warn("Invalid captcha")
            redirectAttributes.addFlashAttribute("error", "验证码错误或已过期")
            return "redirect:/user/register"
        }

        // 检查用户名是否已存在
        if (userService.checkUsernameExists(username)) {
            //logger.warn("Username already exists: $username")
            redirectAttributes.addFlashAttribute("error", "用户名已存在")
            return "redirect:/user/register"
        }

        // 检查昵称是否已存在
        if (userService.checkNicknameExists(nickname)) {
            //logger.warn("Nickname already exists: $nickname")
            redirectAttributes.addFlashAttribute("error", "昵称已存在")
            return "redirect:/user/register"
        }

        // 设置用户信息
        users.name = username
        users.nickName = nickname
        users.password = password

        // 注册用户
        val registeredUser = userService.register(users, username, password, nickname)
        if (registeredUser == null) {
            //logger.warn("Registration failed for user: $username")
            redirectAttributes.addFlashAttribute("error", "注册失败，请稍后再试")
            return "redirect:/user/register"
        }

        // 注册成功，保存用户信息到会话
        //logger.info("Registration successful for user: $username")
        session.setAttribute("user", registeredUser)
        redirectAttributes.addFlashAttribute("success", "注册成功")
        return "redirect:/"
    }

    // 处理更新用户信息请求
    @PostMapping("/update-profile")
    fun updateProfile(
        @RequestParam("nickname") nickname: String?,
        @RequestParam("avatar") avatarFile: MultipartFile?,
        session: HttpSession,
        redirectAttributes: RedirectAttributes
    ): String {
        //logger.info("Processing profile update request")

        val user = session.getAttribute("user") as? UsersDto
        if (user == null) {
            //logger.warn("User not logged in")
            redirectAttributes.addFlashAttribute("error", "请先登录")
            return "redirect:/user/login"
        }

        // 检查昵称是否已锁定
        if (!nickname.isNullOrBlank() && user.isLocked == 1) {
            //logger.warn("Nickname is locked and cannot be changed")
            redirectAttributes.addFlashAttribute("error", "昵称已锁定，不能更改")
            return "redirect:/user/profile"
        }

        // 检查新昵称是否与当前昵称相同
        if (!nickname.isNullOrBlank() && nickname != user.nickName && userService.checkNicknameExists(nickname)) {
            //logger.warn("New nickname already exists: $nickname")
            redirectAttributes.addFlashAttribute("error", "昵称已存在")
            return "redirect:/user/profile"
        }

        // 更新用户信息
        val name = user.name ?: ""
        val token = user.accessToken ?: ""
        val updatedUser = userService.updateUserInfo(name, token, nickname, avatarFile)

        if (updatedUser == null) {
            //logger.warn("Profile update failed for user: ${user.name}")
            redirectAttributes.addFlashAttribute("error", "更新失败，请稍后再试")
            return "redirect:/user/profile"
        }

        // 更新成功，更新会话中的用户信息
        //logger.info("Profile update successful for user: ${user.name}")
        session.setAttribute("user", updatedUser)
        redirectAttributes.addFlashAttribute("success", "更新成功")
        return "redirect:/user/profile"
    }

    // 退出登录
    @GetMapping("/logout")
    fun logout(session: HttpSession, redirectAttributes: RedirectAttributes): String {
        //logger.info("Processing logout request")

        // 清除会话
        session.invalidate()

        redirectAttributes.addFlashAttribute("success", "已退出登录")
        return "redirect:/user/login"
    }
}
