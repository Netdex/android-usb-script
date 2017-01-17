package cf.netdex.hidfuzzer.util;

/**
 * Created by netdex on 1/16/2017.
 */

public interface Func<T> {
    void run(T... p);
}
