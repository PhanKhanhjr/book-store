package phankhanh.book_store.util;
import lombok.experimental.UtilityClass;
import phankhanh.book_store.DTO.request.ReqCreateOrder;
import phankhanh.book_store.domain.Address;
import phankhanh.book_store.util.AddressSnapshot;

@UtilityClass
public class AddressSnapshotMapper {

    // Gộp chuỗi địa chỉ gọn gàng, bỏ null/blank
    private String joinAddress(String line1,  String ward, String district, String province) {
        String[] parts = new String[] {
                safe(line1), safe(ward), safe(district), safe(province)
        };
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p != null && !p.isBlank()) {
                if (!sb.isEmpty()) sb.append(", ");
                sb.append(p.trim());
            }
        }
        return sb.toString();
    }

    private String safe(String s) { return s == null ? null : s.trim(); }

    /** Snapshot từ Address đã lưu của user */
    public AddressSnapshot fromAddress(Address a) {
        if (a == null) return null;
        AddressSnapshot s = new AddressSnapshot();
        s.setReceiverName(a.getFullName());
        s.setReceiverPhone(a.getPhone());
        s.setLine1(a.getLine1());
        s.setWard(a.getWard());
        s.setDistrict(a.getDistrict());
        s.setProvince(a.getProvince());
        s.setAddressLine(joinAddress(a.getLine1(), a.getWard(), a.getDistrict(), a.getProvince()));
        // receiverEmail: m không lưu trong Address => để null
        return s;
    }

    /** Snapshot từ request ad-hoc (khi không có addressId) */
    public AddressSnapshot fromReq(ReqCreateOrder req) {
        if (req == null) return null;
        AddressSnapshot s = new AddressSnapshot();
        s.setReceiverName(safe(req.receiverName()));
        s.setReceiverPhone(safe(req.receiverPhone()));
        s.setLine1(safe(req.line1()));
        s.setWard(safe(req.ward()));
        s.setDistrict(safe(req.district()));
        s.setProvince(safe(req.province()));
        s.setAddressLine(joinAddress(req.line1(), req.ward(), req.district(), req.province()));
        return s;
    }
}

