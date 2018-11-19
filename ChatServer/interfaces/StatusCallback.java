package interfaces;

import java.util.UUID;

import model.Status;

@FunctionalInterface
public interface StatusCallback {
	void call(Status stat, UUID uuid);
}
