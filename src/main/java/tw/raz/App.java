package tw.raz;

import org.apache.commons.lang.*;
import org.slf4j.*;

import java.io.*;
import java.nio.file.*;

import static tw.raz.core.Conv.*;

public class App
{
	private static final Logger log = LoggerFactory.getLogger( App.class );

	public static void main( String[] args )
	{
		try
		{
			String arg = args[ 0 ];

			if ( StringUtils.isBlank( arg ) ) throw new Exception( "argument need o.o" );
			Path path = Paths.get( arg );

			File file = path.toFile();

			if ( !file.exists() ) throw new Exception( "OAO file not exist: " + file.getAbsolutePath() );

			ProcessBy( file );
		}
		catch ( Exception ex )
		{
			log.error( "Failed...=.= " + ex.getMessage(), ex );
		}
	}

}
