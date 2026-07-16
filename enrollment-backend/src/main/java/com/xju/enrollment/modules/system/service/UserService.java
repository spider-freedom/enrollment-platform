package com.xju.enrollment.modules.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xju.enrollment.common.BusinessException;
import com.xju.enrollment.entity.User;
import com.xju.enrollment.mapper.UserMapper;
import com.xju.enrollment.modules.system.dto.ChangePasswordRequest;
import com.xju.enrollment.modules.system.dto.LoginRequest;
import com.xju.enrollment.modules.system.dto.LoginResponse;
import com.xju.enrollment.modules.system.dto.RegisterRequest;
import com.xju.enrollment.modules.system.dto.UserVO;
import com.xju.enrollment.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserMapper userMapper;
    private final JwtUtils jwtUtils;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public LoginResponse login(LoginRequest request) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, request.username())
        );
        if (user == null) {
            throw new BusinessException("该账号不存在，请先注册");
        }
        if ("DISABLED".equals(user.getStatus())) {
            throw new BusinessException("账号已被禁用，请联系管理员");
        }
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException("账号与密码不匹配");
        }
        String token = jwtUtils.generateToken(String.valueOf(user.getId()), user.getRole());
        return new LoginResponse(token, user.getId(), user.getName(), user.getRole(),
                user.getCollegeId(), user.getCollegeName());
    }

    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            throw new BusinessException("旧密码错误");
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userMapper.updateById(user);
    }

    public UserVO getProfile(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return UserVO.from(user);
    }

    @Transactional
    public void updateProfile(Long userId, UserVO vo) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (vo.name() != null) {
            user.setName(vo.name());
        }
        if (vo.major() != null) {
            user.setMajor(vo.major());
        }
        if (vo.grade() != null) {
            user.setGrade(vo.grade());
        }
        if (vo.gpa() != null) {
            user.setGpa(vo.gpa());
        }
        if (vo.email() != null) {
            user.setEmail(vo.email());
        }
        if (vo.phone() != null) {
            user.setPhone(vo.phone());
        }
        if (vo.collegeName() != null) {
            user.setCollegeName(vo.collegeName());
        }
        if (vo.avatar() != null) {
            user.setAvatar(vo.avatar());
        }
        userMapper.updateById(user);
    }

    public User getById(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }

    public List<User> getUsersByCollegeId(Long collegeId) {
        return userMapper.selectList(
                new LambdaQueryWrapper<User>()
                        .eq(User::getCollegeId, collegeId)
        );
    }

    @Transactional
    public void register(RegisterRequest request) {
        // Only STUDENT or TEACHER can register
        if (!"STUDENT".equals(request.role()) && !"TEACHER".equals(request.role())) {
            throw new BusinessException("注册角色仅限学生或教师");
        }

        User existing = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, request.username())
        );
        if (existing != null) {
            throw new BusinessException("用户名已存在");
        }

        User user = new User();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setName(request.name());
        user.setRole(request.role());
        user.setCollegeId(request.collegeId());
        user.setCollegeName(request.collegeName());
        user.setMajor(request.major());
        user.setGrade(request.grade());
        user.setEmail(request.email());
        user.setPhone(request.phone());
        user.setStatus("ACTIVE");

        userMapper.insert(user);
    }

    public List<User> listByCollege(Long collegeId) {
        return userMapper.selectList(
                new LambdaQueryWrapper<User>()
                        .eq(User::getCollegeId, collegeId)
        );
    }

    @Transactional
    public void promoteToAdmin(Long userId, Long operatorCollegeId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (!operatorCollegeId.equals(user.getCollegeId())) {
            throw new BusinessException("无权操作该用户");
        }
        if (!"TEACHER".equals(user.getRole())) {
            throw new BusinessException("仅可提升教师为学院管理员");
        }
        user.setRole("COLLEGE_ADMIN");
        userMapper.updateById(user);
    }

    public void demoteAdmin(Long userId, Long operatorCollegeId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (!operatorCollegeId.equals(user.getCollegeId())) {
            throw new BusinessException("无权操作该用户");
        }
        if (!"COLLEGE_ADMIN".equals(user.getRole())) {
            throw new BusinessException("该用户不是学院管理员");
        }
        user.setRole("TEACHER");
        userMapper.updateById(user);
    }

    @Transactional
    public void deleteUser(Long userId, Long operatorCollegeId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (!operatorCollegeId.equals(user.getCollegeId())) {
            throw new BusinessException("无权操作该用户");
        }
        user.setStatus("DISABLED");
        userMapper.updateById(user);
    }

    @Transactional
    public void toggleUserStatus(Long userId, String status, Long operatorId) {
        User target = userMapper.selectById(userId);
        if (target == null) throw new BusinessException("用户不存在");
        User operator = userMapper.selectById(operatorId);
        if (!"SCHOOL_ADMIN".equals(operator.getRole()) && !operator.getCollegeId().equals(target.getCollegeId())) {
            throw new BusinessException("无权操作该用户");
        }
        if (!"ACTIVE".equals(status) && !"DISABLED".equals(status)) {
            throw new BusinessException("无效的状态值");
        }
        target.setStatus(status);
        userMapper.updateById(target);
    }

    public List<User> listCollegeAdmins(Long collegeId) {
        return userMapper.selectList(
                new LambdaQueryWrapper<User>()
                        .eq(User::getCollegeId, collegeId)
                        .eq(User::getRole, "COLLEGE_ADMIN")
                        .ne(User::getStatus, "DISABLED")
        );
    }

    @Transactional
    public void resetPassword(Long userId, Long operatorId) {
        User target = userMapper.selectById(userId);
        if (target == null) throw new BusinessException("用户不存在");
        User operator = userMapper.selectById(operatorId);
        // Only school admin or same-college college admin can reset
        if (!"SCHOOL_ADMIN".equals(operator.getRole())) {
            if (!operator.getCollegeId().equals(target.getCollegeId())) {
                throw new BusinessException("无权操作其他学院的用户");
            }
        }
        target.setPassword(passwordEncoder.encode("123456"));
        userMapper.updateById(target);
    }

    public Map<String, Integer> importUsers(MultipartFile file, Long operatorId) {
        User operator = userMapper.selectById(operatorId);
        if (operator == null) throw new BusinessException("操作员用户不存在");
        Long collegeId = operator.getCollegeId();
        String collegeName = operator.getCollegeName();
        int success = 0, fail = 0;
        try (var is = file.getInputStream()) {
            var reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(is, java.nio.charset.StandardCharsets.UTF_8));
            String line;
            boolean header = true;
            while ((line = reader.readLine()) != null) {
                if (header) { header = false; continue; }
                try {
                    String[] parts = line.split(",");
                    if (parts.length < 4) { fail++; continue; }
                    User user = new User();
                    user.setUsername(parts[0].trim());
                    user.setName(parts[1].trim());
                    user.setRole(parts[2].trim().toUpperCase()); // STUDENT or TEACHER
                    user.setPassword(passwordEncoder.encode("123456"));
                    if ("STUDENT".equals(user.getRole())) {
                        user.setMajor(parts.length > 3 ? parts[3].trim() : null);
                        user.setGrade(parts.length > 4 ? parts[4].trim() : null);
                    }
                    user.setCollegeId(collegeId);
                    user.setCollegeName(collegeName);
                    user.setStatus("ACTIVE");
                    userMapper.insert(user);
                    success++;
                } catch (Exception e) { fail++; }
            }
        } catch (Exception e) {
            throw new BusinessException("导入失败: " + e.getMessage());
        }
        Map<String, Integer> result = new HashMap<>();
        result.put("success", success);
        result.put("fail", fail);
        return result;
    }

    public List<User> listAll() {
        return userMapper.selectList(null);
    }

    @Transactional
    public void promoteToRole(Long userId, String role) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在");
        if (!"COLLEGE_ADMIN".equals(role) && !"SCHOOL_ADMIN".equals(role))
            throw new BusinessException("无效的角色");
        user.setRole(role);
        userMapper.updateById(user);
    }

    public void demoteToTeacher(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在");
        user.setRole("TEACHER");
        userMapper.updateById(user);
    }

    @Transactional
    public void setUserStatus(Long userId, String status) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在");
        user.setStatus(status);
        userMapper.updateById(user);
    }

    @Transactional
    public void resetPasswordByAdmin(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在");
        user.setPassword(passwordEncoder.encode("123456"));
        userMapper.updateById(user);
    }

    @Transactional
    public void softDeleteUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在");
        user.setStatus("DISABLED");
        userMapper.updateById(user);
    }

    @Transactional
    public void hardDeleteUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在");
        userMapper.deleteById(userId);
    }
}
