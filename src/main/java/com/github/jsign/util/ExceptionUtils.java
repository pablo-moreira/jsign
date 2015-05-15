package com.github.jsign.util;


public class ExceptionUtils {

	public static boolean checkCauseInstanceOfRecursive(Throwable throwable, Class<? extends Throwable> clazz) {
		if (throwable.getCause() == null) {
			return false;
		}
		else if (clazz.isInstance(throwable.getCause())) {
			return true;
		}
		else {
			return checkCauseInstanceOfRecursive(throwable.getCause(), clazz);
		}
	}
}
