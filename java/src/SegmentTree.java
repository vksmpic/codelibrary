import java.util.*;

public class SegmentTree {

	// specific code
	static final int INIT_VALUE = 0;
	static final int NEUTRAL_VALUE = Integer.MIN_VALUE;
	static final int NEUTRAL_DELTA = 0;

	static int joinValues(int leftValue, int rightValue) {
		return Math.max(leftValue, rightValue);
	}

	static int joinDeltas(int oldDelta, int newDelta) {
		return oldDelta + newDelta;
	}

	static int joinValueWithDelta(int value, int delta, int length) {
		return value + delta;
	}

	// generic code
	int n;
	int[] value;
	int[] delta; // affects only child roots

	public SegmentTree(int n) {
		this.n = n;
		value = new int[4 * n];
		delta = new int[4 * n];
		init(0, 0, n - 1);
	}

	public void init(int root, int left, int right) {
		if (left == right) {
			value[root] = INIT_VALUE;
			delta[root] = NEUTRAL_DELTA;
		} else {
			init(2 * root + 1, left, (left + right) / 2);
			init(2 * root + 2, (left + right) / 2 + 1, right);
			value[root] = joinValues(value[2 * root + 1], value[2 * root + 2]);
			delta[root] = NEUTRAL_DELTA;
		}
	}

	void pushDelta(int root, int left, int right) {
		delta[2 * root + 1] = joinDeltas(delta[2 * root + 1], delta[root]);
		delta[2 * root + 2] = joinDeltas(delta[2 * root + 2], delta[root]);
		int middle = (left + right) / 2;
		value[2 * root + 1] = joinValueWithDelta(value[2 * root + 1], delta[root], middle - left + 1);
		value[2 * root + 2] = joinValueWithDelta(value[2 * root + 2], delta[root], right - middle);
		delta[root] = NEUTRAL_DELTA;
	}

	public int query(int a, int b) {
		return query(a, b, 0, 0, n - 1);
	}

	int query(int a, int b, int root, int left, int right) {
		if (a > right || b < left)
			return NEUTRAL_VALUE;
		if (a <= left && right <= b)
			return value[root];
		pushDelta(root, left, right);
		return joinValues(query(a, b, root * 2 + 1, left, (left + right) / 2),
				query(a, b, root * 2 + 2, (left + right) / 2 + 1, right));
	}

	public void modify(int a, int b, int delta) {
		modify(a, b, delta, 0, 0, n - 1);
	}

	void modify(int a, int b, int delta, int root, int left, int right) {
		if (a > right || b < left)
			return;
		if (a <= left && right <= b) {
			this.delta[root] = joinDeltas(this.delta[root], delta);
			value[root] = joinValueWithDelta(value[root], delta, right - left + 1);
			return;
		}
		pushDelta(root, left, right);
		modify(a, b, delta, 2 * root + 1, left, (left + right) / 2);
		modify(a, b, delta, 2 * root + 2, (left + right) / 2 + 1, right);
		value[root] = joinValues(value[2 * root + 1], value[2 * root + 2]);
	}

	// Random test
	public static void main(String[] args) {
		Random rnd = new Random(1);
		for (int step = 0; step < 1000; step++) {
			int n = rnd.nextInt(50) + 1;
			int[] x = new int[n];
			Arrays.fill(x, INIT_VALUE);
			SegmentTree t = new SegmentTree(n);
			for (int i = 0; i < 1000; i++) {
				int b = rnd.nextInt(n);
				int a = rnd.nextInt(b + 1);
				int cmd = rnd.nextInt(3);
				if (cmd == 0) {
					int delta = rnd.nextInt(100) - 50;
					t.modify(a, b, delta);
					for (int j = a; j <= b; j++)
						x[j] = joinValueWithDelta(x[j], delta, 1);
				} else if (cmd == 1) {
					int res1 = t.query(a, b);
					int res2 = x[a];
					for (int j = a + 1; j <= b; j++)
						res2 = joinValues(res2, x[j]);
					if (res1 != res2) {
						System.err.println("error");
						return;
					}
				} else {
					for (int j = 0; j < n; j++) {
						if (t.query(j, j) != x[j]) {
							System.err.println("error");
							return;
						}
					}
				}
			}
		}
		System.out.println("Test passed");
	}
}