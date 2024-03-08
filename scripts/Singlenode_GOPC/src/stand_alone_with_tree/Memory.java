package stand_alone_with_tree;

public class Memory {
	private final static int _SIZE = 500;
	private static final Runtime s_runtime =Runtime.getRuntime ();
	private static long usedMemory ()
	{
		return s_runtime.totalMemory () -  s_runtime.freeMemory ();
		}
	private static void runGC () throws Exception
	{
		long usedMem1 = usedMemory (), usedMem2 = Long.MAX_VALUE;
		for (int i = 0; (usedMem1 < usedMem2) && (i < 500); ++ i)
		{
			s_runtime.runFinalization ();
			s_runtime.gc ();
			Thread.currentThread ().yield ();
			usedMem2 = usedMem1;
			usedMem1 = usedMemory ();
			}
		}
	public static void main( String [] args )throws Exception {
		Integer[] array = new Integer[50];
//		Runtime.getRuntime ().gc();
		long start_total = Runtime.getRuntime().totalMemory();
		long start = Runtime.getRuntime ().freeMemory();
		for (int i = 0; i < 50; i++) {
			array[i] = new Integer(i);
			}
//		Runtime.getRuntime ().gc();
		long end_total = Runtime.getRuntime().totalMemory();
		long end = Runtime.getRuntime ().freeMemory();
		System.out.println(start_total);
		System.out.println(start);
		System.out.println(end_total);
		System.out.println(end);
		}
	}
