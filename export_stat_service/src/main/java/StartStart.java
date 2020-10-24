import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;

/**
 * @description:
 * @author: mryhl
 * @date: Created in 2020/10/13 20:47
 * @version: 1.1
 */
public class StartStart {
	public static void main(String[] args) throws IOException {
		ClassPathXmlApplicationContext act =
				new ClassPathXmlApplicationContext("classpath*:spring/applicationContext-*.xml");

		act.start();

		System.in.read();
	}
}
