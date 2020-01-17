package sb.jafu.app;

import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.context.MessageSourceInitializer;
import org.springframework.boot.autoconfigure.http.HttpProperties;
import org.springframework.boot.autoconfigure.jackson.JacksonInitializer;
import org.springframework.boot.autoconfigure.jackson.JacksonProperties;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.servlet.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.context.support.JafuWebMvcRegistrations;
import org.springframework.context.support.ServletWebServerApplicationContextWithoutSpel;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;
import sb.jafu.app.handler.JafuApplicationRestHandler;
import sb.jafu.app.handler.JafuUserApplicationRestHandler;
import sb.jafu.app.handler.error.*;
import sb.jafu.app.routes.GeneralRoutes;
import sb.jafu.app.routes.UserRoutes;

import java.util.Arrays;
import java.util.function.Supplier;

import static org.springframework.web.servlet.function.RouterFunctions.route;

public class JafuApplication {

	private final ApplicationContextInitializer<GenericApplicationContext> initializer;

	private ServerProperties serverProperties = new ServerProperties();

	private HttpProperties httpProperties = new HttpProperties();

	private WebMvcProperties webMvcProperties = new WebMvcProperties();

	private ResourceProperties resourceProperties = new ResourceProperties();

	protected JafuApplication() {
		this.initializer = context -> {
			new MessageSourceInitializer().initialize(context);
			context.registerBean(CommandLineRunner.class, () -> args -> System.out.println("jafu running!"));
			context.registerBean(BeanDefinitionReaderUtils.uniqueBeanName(GeneralRoutes.class.getName(), context), RouterFunction.class
					, GeneralRoutes.getRoutes().apply(new JafuApplicationRestHandler()));
			context.registerBean(BeanDefinitionReaderUtils.uniqueBeanName(UserRoutes.class.getName(), context), RouterFunction.class
					, UserRoutes.getRoutes().apply(new JafuUserApplicationRestHandler()));

			serverProperties.setPort(8080);
			new StringConverterInitializer().initialize(context);
			new ResourceConverterInitializer().initialize(context);

			new JacksonInitializer(new JacksonProperties()).initialize(context);
			new JacksonJsonConverterInitializer().initialize(context);


			context.registerBean(WebMvcRegistrations.class, JafuWebMvcRegistrations::new);
			context.registerBean(BeanDefinitionReaderUtils.uniqueBeanName(JafuResponseEntityExceptionHandler.class.getName(), context)
					, JafuResponseEntityExceptionHandler.class, () -> new JafuResponseEntityExceptionHandler(context.getEnvironment()));
			webMvcProperties.setThrowExceptionIfNoHandlerFound(true);
			new ServletWebServerInitializer(serverProperties, httpProperties, webMvcProperties, resourceProperties).initialize(context);
		};
	}

	public ConfigurableApplicationContext run() {
		return run("", new String[0]);
	}

	public ConfigurableApplicationContext run(String profiles) {
		return run(profiles, new String[0]);
	}

	public ConfigurableApplicationContext run(String[] args) {
		return run("", args);
	}

	public ConfigurableApplicationContext run(String profiles, String[] args) {
		SpringApplication app = new SpringApplication(JafuApplication.class) {
			@Override
			protected void load(ApplicationContext context, Object[] sources) {
				// We don't want the annotation bean definition reader
			}
		};
		initializeWebApplicationContext(app);
		if (!profiles.isEmpty()) {
			app.setAdditionalProfiles(Arrays.stream(profiles.split(",")).map(it -> it.trim()).toArray(String[]::new));
		}
		app.addInitializers(this.initializer);
		System.setProperty("spring.backgroundpreinitializer.ignore", "true");
		return app.run(args);
	}

	protected void initializeWebApplicationContext(SpringApplication app) {
		app.setWebApplicationType(WebApplicationType.SERVLET);
		app.setApplicationContextClass(ServletWebServerApplicationContextWithoutSpel.class);
	}

	public static void main(String[] args) throws InterruptedException {
		new JafuApplication().run(args);
		Thread.currentThread().join(); // To be able to measure memory consumption
	}
	
}
