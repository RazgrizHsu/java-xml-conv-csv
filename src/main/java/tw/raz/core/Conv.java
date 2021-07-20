package tw.raz.core;

import org.apache.commons.lang.*;
import org.slf4j.*;
import tw.raz.models.*;

import javax.xml.stream.*;
import java.io.*;
import java.nio.charset.*;
import java.util.*;

@SuppressWarnings( "ResultOfMethodCallIgnored" )
public class Conv
{
	private static final Logger log = LoggerFactory.getLogger( Conv.class );

	static Long c = 0L;

	public static void ReadInChildBy( XMLStreamReader reader, String key, IActionMayExBy<Integer> act ) throws Exception
	{
		boolean hasInto = false;
		while ( reader.hasNext() )
		{
			int code = reader.next();
			String name = code == 1 || code == 2 ? reader.getLocalName() : null;

			if ( hasInto && ( code == 1 || code == 2 ) && !name.equals( key ) )
			{
				return;
			}

			if ( code == XMLStreamConstants.END_ELEMENT && name.equals( key ) )
			{
				log.debug( "exit: {}", key );
				break;
			}

			if ( code == XMLStreamConstants.START_ELEMENT && name.equals( key ) )
			{
				hasInto = true;
				act.Run( code );
			}
		}
	}

	public static Boolean IsMatchBy( XMLStreamReader reader, int code, String key )
	{
		return code == XMLStreamConstants.START_ELEMENT && reader.getLocalName().equals( key );
	}

	public static void ReadTextBy( XMLStreamReader reader, int code, String key, IActionMayExBy<String> act )
	{
		if ( code != XMLStreamConstants.START_ELEMENT ) return;
		if ( IsMatchBy( reader, code, key ) )
		{
			try
			{
				String v = reader.getElementText();
				if ( StringUtils.isBlank( v ) ) v = "";
				act.Run( v );
			}
			catch ( Exception ex )
			{
				log.error( "reader: code[" + code + "] now: " + reader.getLocalName() + "", ex );
			}
		}
	}

	public static class Tmp
	{
		public String provider_id = "";
		public String external_id = "";
	}


	public static List<CustomerP1> readP1By( XMLStreamReader reader ) throws Exception
	{
		int rootCode = reader.next();

		if ( rootCode == XMLStreamConstants.START_ELEMENT && reader.getLocalName().equals( "customer" ) )
		{
			c++;
			if ( c % 1000 == 0 ) log.info( "count: {}", c );

			Tmp ref = new Tmp();

			String customer_no = reader.getAttributeValue( 0 );

			LinkedList<CustomerP1> ms = new LinkedList<>();


			while ( reader.hasNext() )
			{
				rootCode = reader.next();

				if ( rootCode == XMLStreamConstants.END_ELEMENT && reader.getLocalName().equals( "customer" ) )
				{
					int size = ms.size();

					//log.info( "customer, ms: {}", size );
					if ( size <= 0 )
					{
						CustomerP1 m = new CustomerP1();
						m.provider_id = ref.provider_id;
						m.external_id = ref.external_id;
						m.customer_no = customer_no;
						ms.add( m );
					}

					return ms;
				}


				if ( rootCode == XMLStreamConstants.START_ELEMENT && reader.getLocalName().equals( "provider-id" ) )
				{
					ref.provider_id = reader.getElementText();
				}
				if ( rootCode == XMLStreamConstants.START_ELEMENT && reader.getLocalName().equals( "external-id" ) )
				{
					ref.external_id = reader.getElementText();
				}

				if ( rootCode == XMLStreamConstants.START_ELEMENT && reader.getLocalName().equals( "addresses" ) )
				{
					ReadInChildBy( reader, "address", ( code ) ->
					{
						CustomerP1 m = new CustomerP1();
						m.provider_id = ref.provider_id;
						m.external_id = ref.external_id;
						m.customer_no = customer_no;

						m.address_id = reader.getAttributeValue( 0 );
						m.preferred = reader.getAttributeValue( 1 );
						//log.info( "address.. into[{}]", m.address_id );

						int cc;
						while ( reader.hasNext() )
						{
							cc = reader.next();

							if ( cc == XMLStreamConstants.END_ELEMENT && reader.getLocalName().equals( "address" ) )
							{
								ms.add( m );
								break;
							}

							ReadTextBy( reader, cc, "salutation", ( v ) -> m.salutation = v );
							ReadTextBy( reader, cc, "title", ( v ) -> m.title = v );
							ReadTextBy( reader, cc, "first-name", ( v ) -> m.first_name = v );
							ReadTextBy( reader, cc, "last-name", ( v ) -> m.last_name = v );
							ReadTextBy( reader, cc, "suffix", ( v ) -> m.suffix = v );
							ReadTextBy( reader, cc, "company-name", ( v ) -> m.company_name = v );
							ReadTextBy( reader, cc, "job-title", ( v ) -> m.job_title = v );
							ReadTextBy( reader, cc, "address1", ( v ) -> m.address1 = v );
							ReadTextBy( reader, cc, "address2", ( v ) -> m.address2 = v );
							ReadTextBy( reader, cc, "suite", ( v ) -> m.suite = v );
							ReadTextBy( reader, cc, "postbox", ( v ) -> m.postbox = v );
							ReadTextBy( reader, cc, "city", ( v ) -> m.city = v );
							ReadTextBy( reader, cc, "postal-code", ( v ) -> m.postal_code = v );
							ReadTextBy( reader, cc, "state-code", ( v ) -> m.state_code = v );
							ReadTextBy( reader, cc, "country-code", ( v ) -> m.country_code = v );
							ReadTextBy( reader, cc, "phone", ( v ) -> m.phone = v );


							if ( cc == XMLStreamConstants.START_ELEMENT && reader.getLocalName().equals( "custom-attribute" ) )
							{
								String attrName = reader.getAttributeLocalName( 0 );
								if ( attrName == null ) continue;

								String attrId = reader.getAttributeValue( 0 );
								String v = reader.getElementText(); if ( StringUtils.isBlank( v ) ) v = "";

								if ( "DeliveryIsElevator".equals( attrId ) ) m.deliveryiselevator = v;
								else if ( "IsDefault".equals( attrId ) ) m.isdefault = v;
								else if ( "isRecipient".equals( attrId ) ) m.isrecipient = v;
							}
						}

						//log.info( "addr: {}", addr );
					} );

					//log.warn( "leave: address" );
				}
			}

		}

		return null;
	}


	public static void ProcessBy( File file ) throws Exception
	{
		int countAll = 0;

		try (
				FileInputStream targetStream = new FileInputStream( file );
				InputStreamReader in = new InputStreamReader( targetStream, StandardCharsets.UTF_8 )
		)
		{

			LinkedList<CustomerP1> ms = new LinkedList<>();

			XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader( in );
			while ( reader.hasNext() )
			{
				//if ( ms.size() >= 2 ) break; //for testing, reduce output

				List<CustomerP1> items = readP1By( reader );
				if ( items != null ) ms.addAll( items );
			}

			countAll += ms.size();
			log.info( "the count from xml: {}", countAll );


			String separator = ",";

			File fileCsv = new java.io.File( file.getAbsolutePath() + ".csv" );
			fileCsv.delete();

			try (
					FileOutputStream fw = new FileOutputStream( fileCsv, true );
					OutputStreamWriter outs = new OutputStreamWriter( fw, StandardCharsets.UTF_8 );
					BufferedWriter bw = new BufferedWriter( outs );
					PrintWriter out = new PrintWriter( bw )
			)
			{

				for ( CustomerP1 m : ms )
				{
					out.print( m.customer_no + separator );
					out.print( m.provider_id + separator );
					out.print( m.external_id + separator );

					out.print( m.address_id + separator );
					out.print( m.preferred + separator );

					out.print( m.salutation + separator );
					out.print( m.title + separator );
					out.print( m.first_name + separator );
					out.print( m.last_name + separator );
					out.print( m.suffix + separator );
					out.print( m.company_name + separator );
					out.print( m.job_title + separator );
					out.print( m.address1 + separator );
					out.print( m.address2 + separator );
					out.print( m.suite + separator );
					out.print( m.postbox + separator );
					out.print( m.city + separator );
					out.print( m.postal_code + separator );
					out.print( m.state_code + separator );
					out.print( m.country_code + separator );
					out.print( m.phone + separator );
					out.print( m.deliveryiselevator + separator );
					out.print( m.isdefault + separator );
					out.print( m.isrecipient );

					out.print( "\n" );

				}

			}

			log.info( "OvO finish, count[{}]", ms.size() );
		}
	}

}
