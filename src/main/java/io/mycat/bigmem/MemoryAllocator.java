package io.mycat.bigmem;

import io.mycat.bigmem.buffer.Arena;
import io.mycat.bigmem.buffer.DirectByteBuffer;
import io.mycat.bigmem.util.UnsafeUtil;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * allocator , recycle and gc all kind of buffers
 * 
 * @author shenli
 *
 */
public class MemoryAllocator {

	final static Logger LOGGER = LoggerFactory.getLogger(MemoryAllocator.class);
	 ByteBuffer buffer;
	/**
	 * 暂不支持MAX_ORDER的设置， 默认为11
	 */
	private static final int DEFAULT_MAX_ORDER = 11;

	/**
	 * 小于4096则算法无法支持
	 */
	private static final int MIN_PAGE_SIZE = 4096;

	/**
	 * 暂不支持DEFAULT_PAGE_SIZE的设置， 默认为8192
	 */
	private static final int DEFAULT_PAGE_SIZE = 8192;

	/**
	 * defaultChunkSize = 16MB
	 */
	final int defaultChunkSize = DEFAULT_PAGE_SIZE << DEFAULT_MAX_ORDER;

	/**
	 * maxNumArenas占用的内存量不能超过系统总内存的50% maxNumArenas包括公共和独占的
	 * 1个Arena内默认最小有3个Chunk:tiny, small and other
	 */
	private static final int MAX_NUM_ARENAS = (int) UnsafeUtil
			.maxDirectMemory() / 2 / 3;

	private static final int DEFAULT_NUM_PUBLIC_ARENAS;

	private static final int DEFAULT_NUM_PRIVATE_ARENAS;

	static {
		final Runtime runtime = Runtime.getRuntime();
		/**
		 * 公共Arena最小数量 https://github.com/netty/netty/issues/3888
		 */
		final int defaultMinNumPublicArena = runtime.availableProcessors() * 2;

		DEFAULT_NUM_PUBLIC_ARENAS = Math.max(0,
				(int) Math.min(defaultMinNumPublicArena, MAX_NUM_ARENAS));

		DEFAULT_NUM_PRIVATE_ARENAS = MAX_NUM_ARENAS - DEFAULT_NUM_PUBLIC_ARENAS;

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("-Dio.mycat.allocator.defaultNumPublicArenas: {}",
					DEFAULT_NUM_PUBLIC_ARENAS);
			LOGGER.debug("-Dio.mycat.allocator.defaultNumPrivateArenas: {}",
					DEFAULT_NUM_PRIVATE_ARENAS);
		}
	}

	public static final MemoryAllocator CURRENT = new MemoryAllocator();

	private final Arena[] publicArenas;
	private final List<Arena> privateArenas = new ArrayList<Arena>(DEFAULT_NUM_PRIVATE_ARENAS);
	
	private MemoryAllocator()
	{
		this(DEFAULT_NUM_PUBLIC_ARENAS, DEFAULT_NUM_PRIVATE_ARENAS);
	}
	
	private MemoryAllocator(int nPublicArena, int nPrivateArena)
	{
		if (nPublicArena > 0) {
            publicArenas = new Arena[nPublicArena]; 
            for (int i = 0; i < publicArenas.length; i ++) {
            	Arena arena = new Arena();
            	publicArenas[i] = arena; 
            }
        }else
        {
        	publicArenas = null;
        }
	}
	
	/**
	 * 申请绑定一个Private Arena。适用于内存需求量大，希望独占Arena，但是所需capacity会动态变化的情况。 绑定后，
	 * 此Arena不能分配其他内存分配请求，也不会与Thread绑定。
	 * 
	 * @param maxCapacity
	 *            预期最大容量
	 * @return privateIdx， -1则表示绑定失败
	 */
	public int allocatePrivate(int maxCapacity) {
		if(validateCapacity(maxCapacity))
		{
			Arena arena = new Arena();
			privateArenas.add(arena);
			return 0;
		}
		return -1;
	}

	/**
	 * 清除Private Arena
	 * 
	 * @param privateIdx
	 * @return
	 */
	public void freePrivate(int privateIdx) {
	}

	public DirectByteBuffer directBuffer(int capacity) {
		return null;
	}

	/**
	 * 在private上分配buff
	 * 
	 * @param privateIdx
	 * @param capacity
	 * @return
	 */
	public DirectByteBuffer directBuffer(int privateIdx, int capacity) {
		return null;
	}
	
	private boolean validateCapacity(int capacity)
	{
		return false;
	}
	//
	// public void recycle(DirectByteBuffer theBuf) ;
}
