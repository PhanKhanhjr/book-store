package phankhanh.book_store.util;

import lombok.experimental.UtilityClass;
import phankhanh.book_store.DTO.response.ResOrderDetail;
import phankhanh.book_store.DTO.response.ResOrderItem;
import phankhanh.book_store.DTO.response.sale.ResOrderAdmin;
import phankhanh.book_store.domain.Order;
import phankhanh.book_store.domain.OrderItem;

import java.util.List;

@UtilityClass
public class OrderMapper {

    public ResOrderDetail toDetail(Order o) {
        var s = o.getShipping();
        return new ResOrderDetail(
                o.getCode(),
                o.getStatus(),
                o.getPaymentStatus(),
                o.getSubtotal(),
                o.getDiscountTotal(),
                o.getShippingFee(),
                o.getTaxTotal(),
                o.getGrandTotal(),

                // shipping snapshot
                s != null ? s.getReceiverName() : null,
                s != null ? s.getReceiverPhone() : null,
                null, // receiverEmail: hệ hiện tại không có -> null
                s != null ? s.getAddressLine() : null,
                s != null ? s.getWard() : null,
                s != null ? s.getDistrict() : null,
                s != null ? s.getProvince() : null,
                null, // postalCode: không dùng

                // timeline
                o.getCreatedAt(),
                o.getConfirmedAt(),
                o.getShippedAt(),
                o.getCompletedAt(),
                o.getCanceledAt(),

                // items
                o.getItems().stream().map(OrderMapper::toItem).toList()
        );
    }

    public ResOrderItem toItem(OrderItem i) {
        return new ResOrderItem(
                i.getBookId(),
                i.getTitleSnapshot(),
                i.getImageUrlSnapshot(), // nhớ bổ sung field này
                i.getSkuSnapshot(),      // và field này nếu m dùng SKU
                i.getPriceSnapshot(),
                i.getDiscountSnapshot(), // 0 nếu không giảm
                i.getQty(),
                i.getLineTotal()
        );
    }
    public final class OrderAdminMapper {
        public static ResOrderAdmin toAdmin(Order o) {
            var addr = o.getShipping();
            var items = o.getItems().stream()
                    .map(OrderMapper::toItem)   // <-- tái sử dụng mapper có sẵn
                    .toList();
            return new ResOrderAdmin(
                    o.getId(), o.getCode(),
                    addr != null ? addr.getReceiverName() : null,
                    addr != null ? addr.getReceiverPhone() : null,
                    addr != null ? addr.getReceiverEmail() : null,
                    addr,
                    o.getStatus(), o.getPaymentStatus(),
                    o.getSubtotal(), o.getShippingFee(), o.getDiscountTotal(), o.getTaxTotal(), o.getGrandTotal(),
                    o.getCreatedAt(), o.getUpdatedAt(),
                    o.getAssigneeName(), o.getAssigneeId(),
                    items
            );
        }
    }


}

