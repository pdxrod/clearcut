package clearcut;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ServletController extends HttpServlet {

	private static clearcut.Injector injector;

	protected void service(HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse) throws IOException,
			ServletException {
		httpServletResponse.getWriter().write("<center>\n");
		try {
			String name = this.getServletName();
			clearcut.Ini.app(name);
			injector = new clearcut.Injector();
			example.biz.IMember member = (example.biz.IMember) injector
					.implement("member");
			String memberName = member.name();
			String actorType = member.actorType();

			httpServletResponse.getWriter().write(
					name + "<br />" + actorType + " " + memberName + "\n");

		} catch (clearcut.InjectionException x) {
			httpServletResponse.getWriter().write(x.getMessage());
		}
		httpServletResponse.getWriter().write("</center>\n");
	}

}
