

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * 文件上传异常类
 * 
 * @author ruoyi
 */
public class FileUploadException extends Exception
{



    public static final String AUTH_MODULE = "console_auth";

    public static final String AUTH_ENABLED = "auth_enabled";

    public static final String LOGIN_PAGE_ENABLED = "login_page_enabled";

    public static final String AUTH_SYSTEM_TYPE = "auth_system_type";

    public static final String AUTH_ADMIN_REQUEST = "auth_admin_request";

    private boolean cacheable;



    private static final long serialVersionUID = 1L;

    private final Throwable cause;

    public FileUploadException()
    {
        this(null, null);
    }

    public FileUploadException(final String msg)
    {
        this(msg, null);
    }

    public FileUploadException(String msg, Throwable cause)
    {
        super(msg);
        this.cause = cause;
    }

    @Override
    public void printStackTrace(PrintStream stream)
    {
        super.printStackTrace(stream);
        if (cause != null)
        {
            stream.println("Caused by:");
            cause.printStackTrace(stream);
        }
    }

    @Override
    public void printStackTrace(PrintWriter writer)
    {
        super.printStackTrace(writer);
        if (cause != null)
        {
            writer.println("Caused by:");
            cause.printStackTrace(writer);
        }
    }

    @Override
    public Throwable getCause()
    {
        return cause;
    }
}
