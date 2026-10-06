// Fixture do PT-02 (docs/dev/12): violação plantada. Código de teste; nunca vai para src/main.
package fixturearquitetura.regra7.violacao.prazos;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DiasUteis {
    int value();
}
