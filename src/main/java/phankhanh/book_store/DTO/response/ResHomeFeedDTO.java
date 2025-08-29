package phankhanh.book_store.DTO.response;

import java.util.List;

public record ResHomeFeedDTO(
        List<ResBookListItemDTO> featuredSale,
        List<ResBookListItemDTO> newArrivals,
        List<ResBookListItemDTO> bestSellers,
        List<CategorySection> byCategories
) {
    public record CategorySection(Long categoryId, String categoryName, List<ResBookListItemDTO> items) {}
}