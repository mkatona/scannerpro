
package hu.u_szeged.scannerpro.model.exceptions;


/**
 * Exception thrown when a commit to the database fails
 *
 * @author krajcsovszkig
 *
 */
public class CommitToDatabaseFailedException extends Exception
{
	private static final long serialVersionUID = -6644528019647863384L;

	/**
	 * 
	 */
	public CommitToDatabaseFailedException()
	{
	}
	
	/**
	 * @param detailMessage
	 */
	public CommitToDatabaseFailedException(String detailMessage)
	{
		super(detailMessage);
	}
	
	/**
	 * @param throwable
	 */
	public CommitToDatabaseFailedException(Throwable throwable)
	{
		super(throwable);
	}
	
	/**
	 * @param detailMessage
	 * @param throwable
	 */
	public CommitToDatabaseFailedException(String detailMessage, Throwable throwable)
	{
		super(detailMessage, throwable);
	}
	
}
