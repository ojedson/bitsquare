/*
 * This file is part of Bitsquare.
 *
 * Bitsquare is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * Bitsquare is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Bitsquare. If not, see <http://www.gnu.org/licenses/>.
 */

package io.bitsquare.arbitration;

import io.bitsquare.app.Version;
import io.bitsquare.common.persistance.Persistable;
import io.bitsquare.storage.Storage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

public final class DisputeList<DisputeCase> extends ArrayList<DisputeCase> implements Persistable {
    // That object is saved to disc. We need to take care of changes to not break deserialization.
    private static final long serialVersionUID = Version.LOCAL_DB_VERSION;

    private static final Logger log = LoggerFactory.getLogger(DisputeList.class);

    final transient private Storage<DisputeList<DisputeCase>> storage;
    transient private ObservableList<DisputeCase> observableList;

    public DisputeList(Storage<DisputeList<DisputeCase>> storage) {
        this.storage = storage;

        DisputeList persisted = storage.initAndGetPersisted(this);
        if (persisted != null) {
            this.addAll(persisted);
        }
        observableList = FXCollections.observableArrayList(this);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        try {
            in.defaultReadObject();
        } catch (Throwable t) {
            log.warn("Cannot be deserialized." + t.getMessage());
        }
    }

    @Override
    public boolean add(DisputeCase disputeCase) {
        if (!super.contains(disputeCase)) {
            boolean result = super.add(disputeCase);
            getObservableList().add(disputeCase);
            storage.queueUpForSave();
            return result;
        } else {
            return false;
        }
    }

    @Override
    public boolean remove(Object disputeCase) {
        boolean result = super.remove(disputeCase);
        getObservableList().remove(disputeCase);
        storage.queueUpForSave();
        return result;
    }

    private ObservableList<DisputeCase> getObservableList() {
        if (observableList == null)
            observableList = FXCollections.observableArrayList(this);
        return observableList;
    }

    @NotNull
    @Override
    public String toString() {
        return "DisputeList{" +
                ", observableList=" + observableList +
                '}';
    }
}
