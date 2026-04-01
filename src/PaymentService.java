public class PaymentService {

    public void pay(String type, int amount) {
        Payment payment = new Payment(type, amount);
        payment.process();
    }
}
