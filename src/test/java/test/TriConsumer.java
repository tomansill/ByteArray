package test;

@FunctionalInterface
public interface TriConsumer<A, B, C>{

  void accept(A one, B two, C three);

}
