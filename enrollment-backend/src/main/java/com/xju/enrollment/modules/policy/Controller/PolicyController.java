package com.xju.enrollment.modules.policy.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xju.enrollment.common.ApiResponse;
import com.xju.enrollment.common.PageResult;
import com.xju.enrollment.entity.Policy;
import com.xju.enrollment.mapper.PolicyMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@RestController
@RequestMapping("/api/policy")
@RequiredArgsConstructor
public class PolicyController {

    private final PolicyMapper policyMapper;

    @GetMapping("/list")
    public ApiResponse<List<Policy>> list() {
        return ApiResponse.ok(policyMapper.selectList(
            new LambdaQueryWrapper<Policy>().orderByDesc(Policy::getCreateTime)));
    }

    @GetMapping("/{id}")
    public ApiResponse<Policy> getById(@PathVariable Long id) {
        return ApiResponse.ok(policyMapper.selectById(id));
    }

    @PostMapping("/create")
    public ApiResponse<Policy> create(@RequestBody Policy policy) {
        policyMapper.insert(policy);
        return ApiResponse.ok(policy);
    }

    @PutMapping("/update/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody Policy policy) {
        policy.setId(id);
        policyMapper.updateById(policy);
        return ApiResponse.ok("更新成功", null);
    }

    @DeleteMapping("/delete/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        policyMapper.deleteById(id);
        return ApiResponse.ok("删除成功", null);
    }

    @PostMapping("/upload")
    public ApiResponse<Map<String,String>> upload(@RequestParam("file") MultipartFile file) {
        try {
            String dir = "./uploads/policies";
            new File(dir).mkdirs();
            String name = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Files.write(Paths.get(dir, name), file.getBytes());
            Map<String,String> r = new HashMap<>();
            r.put("url", "/uploads/policies/" + name);
            return ApiResponse.ok(r);
        } catch (IOException e) {
            return ApiResponse.fail("上传失败");
        }
    }
}