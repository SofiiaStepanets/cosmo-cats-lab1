package lab1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lab1.validation.CosmicWordCheck;

import java.math.BigDecimal;

public class ProductDetailsDTO {

    /** Генерує сервер: у відповіді є, з тіла запиту ігнорується. */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @NotBlank
    @Size(min = 2, max = 50)
    @CosmicWordCheck
    private String name;

    @Size(min = 10, max = 100)
    private String description;

    @NotNull
    @DecimalMin(value = "10.00")
    @Digits(integer = 8, fraction = 2)
    private BigDecimal price;

    @NotBlank
    @Size(max = 50)
    private String category;

    private boolean inStock;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isInStock() {
        return inStock;
    }

    public void setInStock(boolean inStock) {
        this.inStock = inStock;
    }
}
