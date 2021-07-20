package tw.raz.core;

@FunctionalInterface
public interface IActionMayExBy<T>
{
	void Run( T arg ) throws Exception;
}


