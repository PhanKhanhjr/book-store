package phankhanh.book_store.DTO.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import phankhanh.book_store.util.constant.DeliveryMethod;
import phankhanh.book_store.util.constant.PaymentMethod;

public record ReqCreateOrder(
        @NotNull DeliveryMethod deliveryMethod,
        @NotNull PaymentMethod paymentMethod,
        @Size(max = 500) String note,

        // Ưu tiên: dùng địa chỉ đã lưu
        Long addressId,

        // neu chua co dia chi
        @Size(max = 100) String receiverName,
        @Size(max = 20)  String receiverPhone,
        @Size(max = 255) String line1,
        @Size(max = 255) String line2,
        @Size(max = 100) String ward,
        @Size(max = 100) String district,
        @Size(max = 100) String province
) {}
