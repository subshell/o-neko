package io.oneko.helmapi.model;

import com.google.gson.annotations.SerializedName;

public enum ReleaseStatus {
	unknown,
	deployed,
	uninstalled,
	superseded,
	failed,
	uninstalling,
	@SerializedName("pending-install")
	pendingInstall,
	@SerializedName("pending-upgrade")
	pendingUpgrade,
	@SerializedName("pending-rollback")
	pendingRollback
}
