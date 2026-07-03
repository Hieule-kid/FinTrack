package com.fintrack.auth.controller;

import com.fintrack.auth.dto.response.UserResponse;
import com.fintrack.auth.service.AuthService;
import com.fintrack.core.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User profile operations")
public class UserController {

	private final AuthService authService;

	@Operation(
		summary = "Get current user profile",
		description = "Returns the authenticated user's public profile. Requires a valid Bearer JWT.",
		security = @SecurityRequirement(name = "Bearer Authentication")
	)
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Profile returned"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
	})
	@GetMapping("/profile")
	public ResponseEntity<ApiResponse<UserResponse>> getCurrentUserProfile(
			@AuthenticationPrincipal UserDetails userDetails) {
		com.fintrack.auth.model.User user = (com.fintrack.auth.model.User) userDetails;
		UserResponse profile = authService.getProfile(user.getId());
		return ResponseEntity.ok(ApiResponse.success(profile));
	}
}
