package kr.ac.hansung.cse.service;

import kr.ac.hansung.cse.exception.DuplicateCategoryException;
import kr.ac.hansung.cse.model.Category;
import kr.ac.hansung.cse.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * =====================================================================
 * ProductService - 비즈니스 로직 계층 (Service Layer)
 * =====================================================================
 * <p>
 * Service 계층의 역할:
 * - 비즈니스 규칙(Business Logic)을 담당합니다.
 * - Controller와 Repository 사이를 중재합니다.
 * - 여러 Repository 호출을 조합하는 복합 작업을 처리합니다.
 * - 트랜잭션 경계를 정의합니다.
 *
 * @Service: @Component의 특수화입니다.
 * - Spring이 이 클래스를 빈으로 등록합니다.
 * - 비즈니스 로직 담당 클래스임을 의미적으로 명확히 표현합니다.
 * <p>
 * [의존성 주입(Dependency Injection)]
 * 이 클래스는 ProductRepository에 의존합니다.
 * Spring IoC 컨테이너가 ProductRepository 빈을 생성하여 주입해 줍니다.
 * <p>
 * 생성자 주입(Constructor Injection)을 권장하는 이유:
 * 1. 의존성이 명시적으로 드러납니다.
 * 2. 불변(final) 필드로 선언 가능합니다.
 * 3. 단위 테스트 시 목(Mock) 객체 주입이 용이합니다.
 * 4. 순환 의존성을 컴파일 타임에 감지할 수 있습니다.
 * <p>
 * [@Transactional 상세 설명]
 * 클래스 레벨에 선언 시 모든 public 메서드에 트랜잭션이 적용됩니다.
 * <p>
 * 트랜잭션 전파(Propagation):
 * - REQUIRED (기본값): 기존 트랜잭션이 있으면 참여, 없으면 새로 시작
 * - REQUIRES_NEW: 항상 새 트랜잭션 시작 (기존 트랜잭션 일시 중단)
 * - SUPPORTS: 트랜잭션이 있으면 참여, 없으면 없이 실행
 * <p>
 * readOnly = true:
 * - 읽기 전용 최적화: Hibernate의 더티 체킹(변경 감지)을 비활성화합니다.
 * - DB 드라이버 레벨에서 읽기 전용 연결로 설정할 수 있어 성능이 향상됩니다.
 * - 읽기 메서드에는 반드시 readOnly = true를 명시하는 것을 권장합니다.
 */
@Service
@Transactional(readOnly = true) // 클래스 기본값: 읽기 전용 트랜잭션
@RequiredArgsConstructor
public class CategoryService {
	private final CategoryRepository categoryRepository;

	public List<Category> getAllCategories() {
		return categoryRepository.findAll();
	}

	@Transactional // readOnly 오버라이드 → 쓰기 허용
	public Category createCategory(String name) {
		// 중복 검사: 이름이 이미 있으면 예외 발생
		categoryRepository.findByName(name)
				.ifPresent(c -> {
					throw new DuplicateCategoryException(name);
				});
		return categoryRepository.save(new Category(name));
	}

	@Transactional
	public void deleteCategory(Long id) {
		long count = categoryRepository.countProductsByCategoryId(id);
		if (count > 0) throw new IllegalStateException(
				"상품 " + count + "개가 연결되어 있어 삭제할 수 없습니다.");
		categoryRepository.delete(id);
	}
}
