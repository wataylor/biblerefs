package asst.biblerefs;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Empty servlet to make it easy to create a new servlet
 * @author money
 * @since 2020 07
 */
public class GetRefServlet implements javax.servlet.Servlet  {

	static RefSQLLookup rsl;

	@Override
	public void destroy() {
		System.out.println(getClass().getSimpleName() + " destroy called");
	}

	@Override
	public ServletConfig getServletConfig() {
		System.out.println(getClass().getSimpleName() + " getServletConfig called");
		return null;
	}

	@Override
	public String getServletInfo() {
		System.out.println(getClass().getSimpleName() + " getServletInfo called");
		return null;
	}

	@Override
	public void init(ServletConfig arg0) throws ServletException {
		System.out.println(getClass().getSimpleName() + " init called");
	}

	@Override
	public void service(ServletRequest arg0, ServletResponse arg1)
			throws ServletException, IOException {
		HttpServletRequest request = (HttpServletRequest)arg0; 
		HttpServletResponse response = (HttpServletResponse)arg1;
	}

}
