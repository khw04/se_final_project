package com.pokemo.auth.api;

import com.pokemo.auth.service.AdminUserService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

  private final AdminUserService adminUserService;

  public AdminUserController(AdminUserService adminUserService) {
    this.adminUserService = adminUserService;
  }

  @PostMapping("/{id}/promote")
  UserResponse promoteToAdmin(@PathVariable Long id) {
    return adminUserService.promoteToAdmin(id);
  }
}
