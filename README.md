# 📌 3주차 객체지향 & DI 과제

## 1️⃣ 문제 상황 분석

주어진 상황을 코드로 나타내면 다음과 같습니다.

```java
public class NotificationService {
    private EmailSender emailSender = new EmailSender();

    public void notify(String message) {
        emailSender.send(message);
    }
}
```

즉 EmailSender 구현체를 생성하고, notify기능이 이 구현체에 의존하는 형태입니다.

이는 **DIP(의존성 역전 원칙)을** 위반합니다. 따라서 추후 SMS, 푸시 알림 등이 추가될 경우 문제가 발생합니다.

구체적으로 문제를 분석해보면 다음과 같습니다.
#### 1. 구조의 문제점

상위 모듈(NotificationService)이 하위 모듈(EmailSender)의 구체 클래스에 직접 의존 -> EmailSender의 내부 구현이 바뀌면 NotificationService도 함께 수정해야합니다.

#### 2. 추가 기능 개발시

채널이 추가될수록 기존 코드를 직접 수정해야 하므로 구조가 바뀌어 OCP(개방-폐쇄 원칙)도 함께 위반합니다.

```java
public void notify(String type, String message) {
    if (type.equals("email")) {
        new EmailSender().send(message);
    } else if (type.equals("sms")) {
        new SmsSender().send(message);    // 코드 수정
    } else if (type.equals("push")) {
        new PushSender().send(message);   // 또 코드 수정
    }
}
```

채널이 늘어날수록 분기문이 무한히 확장되며, notify를 사용하는 전체 서비스에 대하여 재배포가 필요합니다. 이를 해결하기 위해 인터페이스(추상화)에 의존하도록 하는 의존성 역전이 필요합니다.

---

## 2️⃣ 인터페이스 도입 이유

인터페이스를 도입하여 수정해보겠습니다.

```java
public interface NotificationSender {
    void send(String message);
}

public class EmailSender implements NotificationSender { ... }
public class SmsSender  implements NotificationSender { ... }
public class PushSender  implements NotificationSender { ... } // 여기서 구현

public class NotificationService {
    private NotificationSender sender = new EmailSender();

    public void notify(String message) {
        emailSender.send(message);
    }
}
```

구현체가 아닌 추상화에 의존하므로, 새로운 알림 채널을 추가할 때 NotificationService를 수정할 필요가 없습니다. 즉 기존 코드 추가 없이 새 구현체 추가만으로 확장이 가능하고, NotificationService 내부에서는 각 구현체마다 어떻게 notify를 할 지 구현할 필요가 없습니다.

다만 아직 문제가 남아있습니다. 구현체를 서비스 내부에서 직접 선택한다는 것입니다. 구체적으로 위의 코드에서는

```java
private NotificationSender sender = new EmailSender();
```

이 Line에서 구현체를 선택해서 notify를 사용하고 있습니다. 이렇게 되면 서비스 교체시 여전히 서비스 코드를 직접 수정해야 합니다. 즉 구현체 선택 책임이 서비스에 남아있습니다. 이 문제를 해결하기 위한 개념을 살펴보겠습니다.

---

## 3️⃣ DIP & DI

앞서 문제 상황 분석에서 등장한 DIP를 더 구체적으로 이야기하겠습니다. DIP는 "상위 모듈은 하위 모듈의 구체적인 구현에 의존하면 안 되고, 둘 다 추상화(인터페이스)에 의존해야 한다" 는 원칙입니다. 구체에 의존하는 시스템은 변동성이 크고, 유연성에 약해지기 때문입니다. 즉 추상화를 최대한으로 이용하게 되고, 제어흐름이 소스코드 의존성과 반대방향으로 설계됩니다. 이러한 이유로 의존성 역전이라고 합니다. 위의 예시에서 NotificationService(상위)가 EmailSender(하위 구현체)에 직접 의존하는 게 DIP 위반입니다.

이러한 DIP 원칙을 실현하는 방법이 DI(Dependency Injection)입니다. 즉 위에서 구현체 선택을 서비스에서 했다면, DI는 이 문제를 "외부에서 사용할 구현체를 결정해서 주입한다"는 방식을 사용하는 것입니다.

즉, DIP가 설계의 방향이라면, DI는 그것을 구현하는 방법에 대한 개념입니다. 이 관점에서 객체를 직접 생성하는 방식과 DI 방식을 비교해보면 가장 큰 것은 구현체 선택이 어디에서 이루어지는가(직접 생성: 서비스 내부, DI: 외부에서 결정), 채널 변경시(기능 확장시) 필요한 절차(직접 생성: 서비스 코드 수정 필요, DI: 주입하는 구현체만 수정)가 다릅니다.

---

## 4️⃣ 수동 DI 설계

이제 DI 방싱을 도입해보겠습니다. NotificationService가 구현체를 직접 생성하는 대신 생성자를 통해 생성시점에 외부에서 받도록 변경합니다.

```java
public class NotificationService {
    private final NotificationSender sender;

    public NotificationService(NotificationSender sender) {
        this.sender = sender;
    }

    public void notify(String message) {
        sender.send(message);
    }
}
```

이제 NotificationService 내부에는 new EmailSender() 같은 코드가 완전히 사라졌습니다. 서비스는 NotificationSender 인터페이스만 알고, 무엇을 쓸지는 외부가 결정합니다. 그렇다면 구현체를 결정할 외부는 어떻게 만들면 될까요?

쉽게 이해하기 위해 프로젝트의 구조를 이렇게 예상해봅시다.

```
src/
├── NotificationSender.java   // 인터페이스: 어떻게 보낼지
├── EmailSender.java          // 구현체: 이메일 전송 담당
├── SmsSender.java            // 구현체: SMS 전송 담당
├── PushSender.java           // 구현체: 푸시 알림 전송 담당
├── NotificationService.java  // 서비스: 알림 비즈니스 로직만 담당
├── AppConfig.java            // 설정: 객체 생성 & 의존성 연결 담당
└── Main.java                 // 진입점: 앱 실행
```

이 구조에서 NotificationService의 구현체 생성과 연결은 AppConfig 클래스에서 이루어집니다. 코드로 완성해보면
```java
public class AppConfig {

    public NotificationSender notificationSender() {
        return new SmsSender(); // 채널 선택 Line
    }

    public NotificationService notificationService() {
        return new NotificationService(notificationSender()); // 생성자 호출(구현체 주입)
    }
}
```

이렇게 표현할 수 있습니다. NotificationService 외부에서(AppConfig) 서비스 내에서 사용할 구현체를 결정해주기 때문에(**객체 생성과 의존성 연결이 이루어짐**) DIP를 지킬 수 있게 됩니다. 앱 실행 및 진입점 역할인 Main.java를 간단하게 완성해보면

```java
public class Main {
    public static void main(String[] args) {
        AppConfig config = new AppConfig();
        NotificationService service = config.notificationService();
        service.notify("알림");
    }
}
```

이렇게 완성할 수 있습니다.

거시적인 관점에서 무엇이 변했는지 살펴봅시다. 기존에 NotificationService는 두 가지 책임을 가지고 있었습니다. 어떻게 알림을 보낼지(서비스 로직), 어떤 수단을 쓸지(sender의 타입, 선택). 비즈니스 로직에 해당하는 NotificationService가 알림 수단 교체마다(알림 로직과 별개의 이유) 변경되게 됩니다.

하지만 수동 DI를 적용하여 책임을 서비스 로직 하나로 줄였습니다. AppConfig는 원래부터 "객체를 어떻게 조립할 것인가" 를 책임지는 설정 클래스로 만들었기 때문에, 구현체 결정을 책임지는 게 자연스럽습니다. Main.java는 "앱을 어떻게 실행할 것인가" 라는 진입점 역할에 집중합니다.

즉, 각 클래스가 하나의 적절한 책임을 지게 되고, 수정이 필요한 시점이 적절한 하나의 이유로 고정됩니다. 자연스럽게 단일 책임 원칙(SRP) 도 지켜집니다.

---
## 5️⃣ Spring DI

하지만 수동으로 이렇게 DI를 사용하는 건 한계가 있습니다. 규모가 커질수록 AppConfig가 감당해야 할 구현체 조립이 폭발적으로 증가하고, 결국 클래스의 생성과 연결은 개발자의 몫입니다. 또 매번 new로 생성하기 때문에 메모리 관리 문제에도 힘을 써야합니다.

따러서 우리는 웹 프레임워크, 그중에서도 Java기반의 Spring을 사용해 위의 문제를 해결할 수 있습니다. Spring은 제어권을 프레임워크가 가지는 IoC 방식을 사용합니다. 즉 프레임워크가 필요에 따라 사용자의 코드를 호출하는데, 이 방식으로 서비스간 의존성이 존재하는 경우 객체의 생성과 관리를 Spring이 책임지는 형태입니다. 이를 Bean이라는 이름으로 Java 객체의 생명 주기를 관리합니다. 

즉 우리의 예시에서 AppConfig를 대신 수행하는 것입니다. 또한 기본적으로 싱글톤으로(특정 클래스의 인스턴스를 하나만 생성해서 재사용) 객체를 관리해 동일한 빈을 여러 곳에서 주입받아도 같은 인스턴스를 재사용합니다.

Spring의 DI를 위한 핵심 어노테이션은 3가지가 있습니다.

@Configuration — "이 클래스는 Spring 설정 파일이다"라고 선언합니다. 기존 수동 DI의 AppConfig.java에 해당하는 역할입니다.

@Bean — @Configuration 클래스 안의 메서드에 붙여서, 해당 메서드가 반환하는 객체를 Spring 컨테이너에 빈으로 등록합니다. 수동 DI에서 개발자가 직접 new로 생성하던 코드를 Spring이 대신 관리하게 됩니다.

@ComponentScan — @Bean을 일일이 등록하는 것도 클래스가 많아지면 번거롭습니다. @Component, @Service 등의 어노테이션이 붙은 클래스를 지정된 패키지에서 자동으로 스캔해 빈으로 등록해주는 역할입니다.

프로젝트 구조가 똑같다고 가정하면 위 방식을 다음과 같이 구현할 수 있습니다.

#### 1. Bean + Configuration

```java
@Configuration // 클래스 선언
public class AppConfig {

    @Bean // 객체 관리
    public NotificationSender notificationSender() {
        return new SmsSender();
    }

    @Bean
    public NotificationService notificationService() {
        return new NotificationService(notificationSender());
    }
}
```

#### 2. ComponentScan

```java
// SmsSender.java
@Component //Repository, Controller 등도 사용 가능
public class SmsSender implements NotificationSender { ... }

// NotificationService.java
@Service
public class NotificationService {

    private final NotificationSender sender;

    @Autowired // 구현체 자동 주입(생성자가 1개라 없어도 되지만, 2개 이상이면 사용)
    public NotificationService(NotificationSender sender) {
        this.sender = sender;
    }
}

// AppConfig.java
@Configuration
@ComponentScan("스캔할 패키지 경로")
public class AppConfig {}
```

---

## 6️⃣ 느낀 점

위에서 간단하게 IoC, DI, 싱글톤, 인터페이스 개념이 언급되었습니다. 그 중에서도 IoC가 Spring을 써야하는 이유로 와닿아서 더 정리해보겠습니다.

IoC(Inversion of Control) - 기존에는 개발자가 직접 new 코드를 사용해 객체의 생성, 의존성 연결, 생명주기를 관리했다면, 그 모든 제어권을 Spring 컨테이너로 넘기는 것입니다. 이 때 Spring의 컨테이너는 객체(Bean)의 생성과 생명주기를 관리하는 소프트웨어의 구성요소로 Bean을 담아두고, 필요할 때 꺼내주는 역할을 하기 때문에 컨테이너라고 부릅니다.

우리는 어노테이션을 통해 Spring이 알아서 객체를 만들어서 나에게 주는 제어권 역전이 일어나는 것입니다. 즉 이를 통해 개념을 총정리 해보면,

IoC - "제어권을 프레임워크에게 넘긴다", DI - "IoC를 구현", Spring - "DI 자동화"

즉, IoC라는 원칙이 있음 → "제어권을 개발자가 아닌 외부에 넘겨라" -> Spring 컨테이너는 IoC 원칙 위에서 DI를 자동화. 이렇게 이해했습니다.

과제를 진행하면서 실습 시간에는 개념은 이해가 되어도, 코드를 이렇게 수정하는게 왜 유연성이 확장되는 거고, 원칙을 지키게 되는건지 의아한 부분이었습니다. 하지만 제가 직접 에제를 만들며 코드의 수정이 필요한 이유를 순서대로 생각하였습니다. 이렇게 정리하니 기능의 확장과 수정 관점에서 원칙과 기술의 도입이 더 쉽게 이해되었습니다. 특히 개념에서 원칙과 구현을 나눠서 정리할 수 있는 시간이었습니다. 

하지만 세션에서도 언급되었던 원칙의 충돌부분도 계속 고민할거리로 남아있습니다. 특히 수동 DI에서 언급한 것 처럼 SRP를 고집하다보면 프로젝트 구조에서 계층이 계속해서 증가할 것 같은데 이를 절충하는 지점이 어디일지, 오버엔지니어링과 유연성의 합의 지점을 개발자가 알아야할 것 같습니다. 이 부분이 경험의 차이이지 않을까 싶습니다.

Spring이 필요한 이유와 코드의 설계를 다뤄보았으니 이제 이를 실제로 적용해볼 다음 세션들이 기대됩니다.