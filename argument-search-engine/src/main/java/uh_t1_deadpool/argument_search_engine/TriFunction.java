package uh_t1_deadpool.argument_search_engine;

@FunctionalInterface
public interface TriFunction<A, B, C, D>
{
	public D apply(A a, B b, C c);
}
