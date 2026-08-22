package com.fintrack.auth.controller;

import com.fintrack.auth.dto.request.UpdateCurrencyRequest;
import com.fintrack.auth.dto.request.UpdateUserProfileRequest;
import com.fintrack.auth.dto.response.UserResponse;
import com.fintrack.auth.model.User;
import com.fintrack.auth.service.AuthService;
import com.fintrack.core.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
		UserResponse profile = authService.getProfile(currentUserId(userDetails));
		return ResponseEntity.ok(ApiResponse.success(profile));
	}

	@Operation(
		summary = "Update current user profile",
		description = "Replaces the authenticated user's full name, email, and currency. Requires a valid Bearer JWT.",
		security = @SecurityRequirement(name = "Bearer Authentication")
	)
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Profile updated"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Missing or invalid JWT"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Email already in use")
	})
	@PutMapping("/profile")
	public ResponseEntity<ApiResponse<UserResponse>> updateCurrentUserProfile(
			@AuthenticationPrincipal UserDetails userDetails,
			@Valid @RequestBody UpdateUserProfileRequest request) {
		UserResponse profile = authService.updateProfile(currentUserId(userDetails), request);
		return ResponseEntity.ok(ApiResponse.success(profile));
	}

	@Operation(
		summary = "Update current user's currency",
		description = "Partially updates only the authenticated user's preferred currency. Requires a valid Bearer JWT.",
		security = @SecurityRequirement(name = "Bearer Authentication")
	)
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Currency updated"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
	})
	@PatchMapping("/currency")
	public ResponseEntity<ApiResponse<UserResponse>> updateCurrentUserCurrency(
			@AuthenticationPrincipal UserDetails userDetails,
			@Valid @RequestBody UpdateCurrencyRequest request) {
		UserResponse profile = authService.updateCurrency(currentUserId(userDetails), request);
		return ResponseEntity.ok(ApiResponse.success(profile));
	}

	private String currentUserId(UserDetails userDetails) {
		return ((User) userDetails).getId();
	}
}
