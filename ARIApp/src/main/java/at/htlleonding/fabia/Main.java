package at.htlleonding.fabia;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import jakarta.inject.Inject;

@QuarkusMain
public class Main implements QuarkusApplication {
    @Inject
    AriContext ariContext;

    @Override
    public int run(String... args) {
        Quarkus.waitForExit();
        return 0;
    }
}