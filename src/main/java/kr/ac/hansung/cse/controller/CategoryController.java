package kr.ac.hansung.cse.controller;

import jakarta.validation.Valid;
import kr.ac.hansung.cse.exception.DuplicateCategoryException;
import kr.ac.hansung.cse.exception.ProductNotFoundException;
import kr.ac.hansung.cse.model.CategoryForm;
import kr.ac.hansung.cse.model.Product;
import kr.ac.hansung.cse.model.ProductForm;
import kr.ac.hansung.cse.service.CategoryService;
import kr.ac.hansung.cse.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * =====================================================================
 * ProductController - 웹 요청 처리 계층 (Controller Layer)
 * =====================================================================
 * <p>
 * MVC 패턴에서 Controller의 역할:
 * 1. HTTP 요청을 받아 적절한 Service 메서드를 호출합니다.
 * 2. Service로부터 받은 결과를 Model에 담아 View에 전달합니다.
 * 3. 어떤 View를 렌더링할지 결정하여 뷰 이름을 반환합니다.
 * <p>
 * [엔드포인트 목록]
 * GET  /categories             → 카테고리 목록 조회 (categoryList.html 신규 작성)
 * GET  /categories/create      → 카테고리 등록 폼 표시
 * POST /categories/create      → 카테고리 등록 처리(@Valid + BindingResult)
 * POST /categories/{id}/delete → 카테고리 삭제(연결된 상품 있는 경우 예외 처리)
 */
@Controller
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {
	private final CategoryService categoryService;

	// ─────────────────────────────────────────────────────────────────
	// GET  /categories             → 카테고리 목록 조회 (categoryList.html 신규 작성)
	// ─────────────────────────────────────────────────────────────────

	@GetMapping // GET /categories → 목록
	public String listCategories(Model model) {
		model.addAttribute("categories", categoryService.getAllCategories());
		return "categoryList";
	}
	// ─────────────────────────────────────────────────────────────────
	// GET  /categories/create      → 카테고리 등록 폼 표시
	// ─────────────────────────────────────────────────────────────────

	@GetMapping("/create") // GET → 등록 폼 표시
	public String showCreateForm(Model model) {
		model.addAttribute("categoryForm", new CategoryForm());
		return "categoryForm";
	}

	// ─────────────────────────────────────────────────────────────────
	// POST /categories/create      → 카테고리 등록 처리(@Valid + BindingResult)
	// ─────────────────────────────────────────────────────────────────

	@PostMapping("/create") // POST → 등록 처리
	public String createCategory(
			@Valid @ModelAttribute CategoryForm categoryForm,
			BindingResult bindingResult,
			RedirectAttributes redirectAttributes) {
		if (bindingResult.hasErrors()) return "categoryForm"; // 검증 실패
		try {
			categoryService.createCategory(categoryForm.getName());
			redirectAttributes.addFlashAttribute("successMessage", "등록 완료");
		} catch (DuplicateCategoryException e) {
			// 중복 예외 → BindingResult에 필드 오류 등록 후 폼 재표시
			bindingResult.rejectValue("name", "duplicate", e.getMessage());
			return "categoryForm";
		}
		return "redirect:/categories";
	}

	// ─────────────────────────────────────────────────────────────────
	// POST /categories/{id}/delete → 카테고리 삭제(연결된 상품 있는 경우 예외 처리)
	// ─────────────────────────────────────────────────────────────────

	@PostMapping("/{id}/delete")
	public String deleteCategory(@PathVariable Long id,
								 RedirectAttributes redirectAttributes) {
		try {
			categoryService.deleteCategory(id);
			redirectAttributes.addFlashAttribute("successMessage", "삭제 완료");
		} catch (IllegalStateException e) {
			// 연결된 상품 있을 때 → Flash로 오류 메시지 전달
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
		}
		return "redirect:/categories";
	}
}
