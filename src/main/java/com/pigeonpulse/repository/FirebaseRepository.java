package com.pigeonpulse.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.Map;
import java.util.HashMap;

@Repository
public abstract class FirebaseRepository<T> {

    @Autowired
    protected Firestore firestore;

    protected abstract String getCollectionName();
    protected abstract Class<T> getEntityClass();

    public Optional<T> findById(String id) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection(getCollectionName()).document(id);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();
        if (document.exists()) {
            T entity = document.toObject(getEntityClass());
            return Optional.ofNullable(entity);
        }
        return Optional.empty();
    }

    public List<T> findAll() throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = firestore.collection(getCollectionName()).get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        return documents.stream()
                .map(doc -> doc.toObject(getEntityClass()))
                .toList();
    }

    public String save(T entity) throws ExecutionException, InterruptedException {
        System.out.println("FirebaseRepository: Saving entity to collection: " + getCollectionName());
        System.out.println("FirebaseRepository: Entity: " + entity.toString());
        CollectionReference collection = firestore.collection(getCollectionName());
        ApiFuture<DocumentReference> future = collection.add(entity);
        DocumentReference document = future.get();
        String id = document.getId();
        System.out.println("FirebaseRepository: Saved with ID: " + id);
        return id;
    }

    public void update(String id, T entity) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection(getCollectionName()).document(id);
        ApiFuture<WriteResult> future = docRef.set(entity);
        future.get();
    }

    public void deleteById(String id) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection(getCollectionName()).document(id);
        ApiFuture<WriteResult> future = docRef.delete();
        future.get();
    }

    public List<T> findByField(String field, Object value) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = firestore.collection(getCollectionName())
                .whereEqualTo(field, value)
                .get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        return documents.stream()
                .map(doc -> doc.toObject(getEntityClass()))
                .toList();
    }

    public void removeFieldFromDocument(String id, String fieldName) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection(getCollectionName()).document(id);
        Map<String, Object> updates = new HashMap<>();
        updates.put(fieldName, FieldValue.delete());
        ApiFuture<WriteResult> future = docRef.update(updates);
        future.get();
    }

    public void removeFieldFromAllDocuments(String fieldName) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = firestore.collection(getCollectionName()).get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();

        for (QueryDocumentSnapshot document : documents) {
            if (document.contains(fieldName)) {
                Map<String, Object> updates = new HashMap<>();
                updates.put(fieldName, FieldValue.delete());
                ApiFuture<WriteResult> updateFuture = document.getReference().update(updates);
                updateFuture.get();
            }
        }
    }
}