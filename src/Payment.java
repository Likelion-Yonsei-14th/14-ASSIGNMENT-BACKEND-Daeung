public class Payment {
    private final String type;
    private final int amount;

    public Payment(String type, int amount) {
        this.type = type;
        this.amount = amount;
    }

    public void process() {
        if (amount <= 0) {
            System.out.println("금액이 올바르지 않습니다.");
            return;
        }
        if (isCard()) {
            System.out.println("카드 결제: " + amount);
        } else if (isKakao()) {
            System.out.println("카카오페이 결제: " + amount);
        } else {
            System.out.println("지원하지 않는 결제 방식");
        }
    }

    public boolean isCard() {
        return "card".equals(this.type);
    }

    public boolean isKakao() {
        return "kakao".equals(this.type);
    }
}
