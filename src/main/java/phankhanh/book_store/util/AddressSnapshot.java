package phankhanh.book_store.util;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter @Setter
public class AddressSnapshot {
    @Column(length = 100) private String receiverName;
    @Column(length = 20)  private String receiverPhone;
    @Column(length = 120) private String receiverEmail;


    @Column(length = 255) private String line1;
    @Column(length = 255) private String line2;
    @Column(length = 100) private String ward;
    @Column(length = 100) private String district;
    @Column(length = 100) private String province;

    @Column(length = 512) private String addressLine;
}
