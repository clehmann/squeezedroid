package net.chrislehmann.squeezedroid.exception;


/*
 * Base class for application based runtime exceptions
 */
public class ApplicationException extends RuntimeException
{
   public ApplicationException(String message, Exception e)
   {
      super( message, e );
   }

   public ApplicationException(String string)
   {
      super( string );
   }

   private static final long serialVersionUID = 1L;

}
