package com.example;
import java.util.concurrent.TimeUnit;
import io.reactivex.Observable;
import java.util.Random;

class FootballEvent {
    private String eventType;
    private String description;

    public FootballEvent(String eventType, String description) {
        this.eventType = eventType;
        this.description = description;
    }

    public String getEventType() {
        return eventType;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "Event: " + eventType + " - " + description;
    }
}



class FootballMatch {

    public Observable<FootballEvent> streamEvents() {
        return Observable.interval(500, TimeUnit.MILLISECONDS) // Emite eventos cada 500 ms
                .map(this::generateEvent)                      // Genera un evento a partir del índice
                .take(20);                                     // Limita a 20 eventos
    }

    private FootballEvent generateEvent(Long index) {
        String[] eventTypes = {"GOAL", "FOUL", "CARD", "ASSIST", "OFFSIDE"};
        String eventType = eventTypes[new Random().nextInt(eventTypes.length)];
        return new FootballEvent(eventType, "Generated event #" + index);
    }
}

public class App 
{
    public static void main(String[] args) {
        FootballMatch match = new FootballMatch();

        Observable<FootballEvent> events = match.streamEvents();

        events
            .filter(event -> !event.getEventType().equals("OFFSIDE")) // 1. Filtrar "OFFSIDE"
            .map(event -> new FootballEvent(                         // 2. Transformar descripción
                    event.getEventType(),
                    event.getDescription().toUpperCase()
            ))
            .distinctUntilChanged(event -> event.getEventType())     // 3. Ignorar tipos de evento repetidos copnsecutivamente
            .buffer(2)                                               // 4. Agrupar en lotes de 3 eventos
            .flatMap(buffer -> Observable.fromIterable(buffer)       // 5. Emitir cada evento del buffer
                    .delay(500, TimeUnit.MILLISECONDS))              // Introducir un pequeño retraso
            .subscribe(
                    event -> System.out.println("[Subscriber] " + event),
                    error -> System.err.println("[Error] " + error),
                    () -> System.out.println("[Complete] No more events.")
            );

        // Evitar que el programa termine antes de procesar todos los eventos
        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
