package com.driver.services;

import com.driver.EntryDto.AddTrainEntryDto;
import com.driver.EntryDto.SeatAvailabilityEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Station;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.*;

@Service
public class TrainService {

    @Autowired
    TrainRepository trainRepository;

    public Integer addTrain(AddTrainEntryDto trainEntryDto) {

        //Add the train to the trainRepository
        //and route String logic to be taken from the Problem statement.
        //Save the train and return the trainId that is generated from the database.
        //Avoid using the lombok library
        Train train = new Train();
        List<Station> stations = trainEntryDto.getStationRoute();
        String route = "";
        int listsize = stations.size();
        for (int i = 0; i < listsize; i++) {
            if (i != listsize - 1) {
                route += stations.get(i) + ",";
            } else {
                route += stations.get(i);
            }
        }
        train.setRoute(route);
        train.setNoOfSeats(trainEntryDto.getNoOfSeats());
        train.setDepartureTime(trainEntryDto.getDepartureTime());
        train.setBookedTickets(new ArrayList<>());

        Train train1 = trainRepository.save(train);

        return train1.getTrainId();

    }

    public Integer calculateAvailableSeats(SeatAvailabilityEntryDto seatAvailabilityEntryDto) {

        //Calculate the total seats available
        //Suppose the route is A B C D
        //And there are 2 seats avaialble in total in the train
        //and 2 tickets are booked from A to C and B to D.
        //The seat is available only between A to C and A to B. If a seat is empty between 2 station it will be counted to our final ans
        //even if that seat is booked post the destStation or before the boardingStation
        //Inshort : a train has totalNo of seats and there are tickets from and to different locations
        //We need to find out the available seats between the given 2 stations.
        Optional<Train> trainOptional = trainRepository.findById(seatAvailabilityEntryDto.getTrainId());
        Train train = trainOptional.get();

        Station fromStation = seatAvailabilityEntryDto.getFromStation();
        Station toStation = seatAvailabilityEntryDto.getToStation();

        int totalSeats = train.getNoOfSeats();
        int bookedSeats = 0;

        for (Ticket ticket : train.getBookedTickets()) {
            if ((ticket.getFromStation().ordinal() >= fromStation.ordinal() && ticket.getFromStation().ordinal() <= toStation.ordinal()) || (ticket.getToStation().ordinal() >= fromStation.ordinal() && ticket.getToStation().ordinal() <= toStation.ordinal())) {
                bookedSeats += 1;
            }
        }
        return totalSeats - bookedSeats;
    }

    public Integer calculatePeopleBoardingAtAStation(Integer trainId, Station station) throws Exception {

        //We need to find out the number of people who will be boarding a train from a particular station
        //if the trainId is not passing through that station
        //throw new Exception("Train is not passing from this station");
        //  in a happy case we need to find out the number of such people.

        Optional<Train> trainOptional = trainRepository.findById(trainId);
        if (trainOptional.isPresent() == false) {
            throw new Exception("Train is not passing from this station");
        }
        Train train = trainOptional.get();
        List<Ticket> ticketList = train.getBookedTickets();

        int people = 0;
        for (Ticket ticket : ticketList) {
            Station ticketFromStation = ticket.getFromStation();
            if (ticketFromStation.equals(station)) {
                people++;
            }
        }


        return people;
    }

    public Integer calculateOldestPersonTravelling(Integer trainId) {

        //Throughout the journey of the train between any 2 stations
        //We need to find out the age of the oldest person that is travelling the train
        //If there are no people travelling in that train you can return 0

        int people = 0;
        Optional<Train> trainOptional = trainRepository.findById(trainId);
        int max = 0;
        Train train = trainOptional.get();

        List<Ticket> ticketList = train.getBookedTickets();

        for (Ticket ticket : ticketList) {
            for (Passenger passenger : ticket.getPassengersList()) {
                if (passenger.getAge() > max) {
                    max = passenger.getAge();
                    people = max;
                }
            }
        }


        return people;
    }

    public List<Integer> trainsBetweenAGivenTime(Station station, LocalTime startTime, LocalTime endTime) {

        //When you are at a particular station you need to find out the number of trains that will pass through a given station
        //between a particular time frame both start time and end time included.
        //You can assume that the date change doesn't need to be done ie the travel will certainly happen with the same date (More details
        //in problem statement)
        //You can also assume the seconds and milli seconds value will be 0 in a LocalTime format.

        List<Train> trains = trainRepository.findAll();

        List<Integer> result = new ArrayList<>();

        for (Train train : trains) {
            String route = train.getRoute();

            Set<Station> stationList = getRouteStations(route);

            LocalTime trainDepartureTime = train.getDepartureTime();
            LocalTime arrivalTimeAtStation = trainDepartureTime.plusHours(hoursBetweenStations((List<Station>) stationList, station));

            if (stationList.contains(station) &&
                    !arrivalTimeAtStation.isBefore(startTime) &&
                    !arrivalTimeAtStation.isAfter(endTime)) {
                result.add(train.getTrainId());
            }


        }
        return result;
    }

    private long hoursBetweenStations(List<Station> stationList, Station station) {
        // Find the index of the target station in the route
        int targetIndex = stationList.indexOf(station);

        // Calculate the number of hours it takes to travel from the first station to the target station
        return targetIndex * 1; // Assuming 1 hour between each station
    }

    private Set<Station> getRouteStations(String route) {
        Set<Station> stationSet = EnumSet.noneOf(Station.class);

        // Split the route string into station names
        String[] stationNames = route.split(",");

        for (String stationName : stationNames) {
            stationSet.add(Station.valueOf(stationName.trim()));
        }

        return stationSet;
    }
}



