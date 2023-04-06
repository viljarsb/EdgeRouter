package Misc;

public class MMTPValidationException extends Exception
{
    public MMTPValidationException(String message)
    {
        super(message);
    }

    public MMTPValidationException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public MMTPValidationException(Throwable cause)
    {
        super(cause);
    }
}
