package com.movieticket.service;

import com.movieticket.model.Booking;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for object serialization and deserialization of booking archives.
 * Satisfies Module 4 Serialization requirements (ObjectOutputStream, ObjectInputStream, Serializable).
 */
public class SerializationService {

    private static final String DEFAULT_ARCHIVE_PATH = "exports/bookings.dat";

    private SerializationService() {}

    /**
     * Serializes a list of Booking objects to the specified archive file.
     * @param bookings the list of bookings to serialize
     * @param filePath destination file path
     * @return File reference
     * @throws IOException on serialization error
     */
    public static File exportBookingsToDat(List<Booking> bookings, String filePath) throws IOException {
        File file = new File(filePath);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            // Write ArrayList of serializable bookings
            oos.writeObject(new ArrayList<>(bookings));
            oos.flush();
        }

        return file;
    }

    public static File exportBookingsToDat(List<Booking> bookings) throws IOException {
        return exportBookingsToDat(bookings, DEFAULT_ARCHIVE_PATH);
    }

    /**
     * Deserializes and inspects bookings from an archive file without overwriting DB.
     * @param filePath archive file path
     * @return list of deserialized bookings
     * @throws IOException on file error
     * @throws ClassNotFoundException on class mismatch
     */
    @SuppressWarnings("unchecked")
    public static List<Booking> inspectBookingsFromDat(String filePath)
            throws IOException, ClassNotFoundException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IOException("Archive file does not exist: " + filePath);
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Object obj = ois.readObject();
            if (obj instanceof List<?>) {
                return (List<Booking>) obj;
            } else {
                throw new IOException("Unexpected content type inside archive file: " + obj.getClass());
            }
        }
    }

    public static List<Booking> inspectBookingsFromDat() throws IOException, ClassNotFoundException {
        return inspectBookingsFromDat(DEFAULT_ARCHIVE_PATH);
    }
}
