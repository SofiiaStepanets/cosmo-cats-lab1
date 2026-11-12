package lab1.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/** Тіло POST /api/v1/orders: позиції замовлення та імена котиків, які його роблять. */
public record PlaceOrderRequest(
        @NotEmpty List<@Valid Item> items,
        @NotEmpty List<@NotBlank String> catsNames) {

    public record Item(@NotNull Long productId, @Min(1) int quantity) {
    }
}
