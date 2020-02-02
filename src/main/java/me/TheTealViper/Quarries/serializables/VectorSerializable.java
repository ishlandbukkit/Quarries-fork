package me.TheTealViper.Quarries.serializables;

import org.bukkit.util.Vector;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class VectorSerializable implements Serializable {
    private static final long serialVersionUID = -3272399987804008132L;

    final double x;
    final double y;
    final double z;

    public VectorSerializable(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static VectorSerializable parseVector(Vector vec) {
        return new VectorSerializable(vec.getX(), vec.getY(), vec.getZ());
    }

    public Vector toVector() {
        return new Vector(x, y, z);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }

}
