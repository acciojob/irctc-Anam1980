package com.driver.services;


import com.driver.EntryDto.BookTicketEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Station;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.PassengerRepository;
import com.driver.repository.TicketRepository;
import com.driver.repository.TrainRepository;
import org.assertj.core.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TicketService {

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    TrainRepository trainRepository;

    @Autowired
    PassengerRepository passengerRepository;


    public Integer bookTicket(BookTicketEntryDto bookTicketEntryDto)throws Exception{

        //Check for validity
        //Use bookedTickets List from the TrainRepository to get bookings done against that train
        // Incase the there are insufficient tickets
        // throw new Exception("Less tickets are available");
        //otherwise book the ticket, calculate the price and other details
        //Save the information in corresponding DB Tables
        //Fare System : Check problem statement
        //Incase the train doesn't pass through the requested stations
        //throw new Exception("Invalid stations");
        //Save the bookedTickets in the train Object
        //Also in the passenger Entity change the attribute bookedTickets by using the attribute bookingPersonId.
       //And the end return the ticketId that has come from db
        Optional<Train>trainOptional=trainRepository.findById(bookTicketEntryDto.getTrainId());

            Train train = trainOptional.get();
            String routeStations = train.getRoute();

            List<Object> routeStationsList = Arrays.asList(routeStations.split(","));

            String fromStation = String.valueOf(bookTicketEntryDto.getFromStation());
            String toStation = String.valueOf(bookTicketEntryDto.getToStation());

            if(!routeStationsList.contains(fromStation) || !routeStationsList.contains(toStation)){
                throw  new Exception("Invalid stations");
            }

            int availableSeats = train.getNoOfSeats()-train.getBookedTickets().size();
            int requestedSeats = bookTicketEntryDto.getNoOfSeats();

            if(availableSeats>=requestedSeats){
                Ticket ticket = createTicket(train, bookTicketEntryDto);
                Ticket ticket1=ticketRepository.save(ticket);

                List<Ticket>tickets = train.getBookedTickets();
                tickets.add(ticket1);
                train.setBookedTickets(tickets);
                trainRepository.save(train);

                int id = bookTicketEntryDto.getBookingPersonId();
                Optional<Passenger> passenger = passengerRepository.findById(id);
                if(passenger.isPresent()){
                    Passenger passenger1 = passenger.get();
                    List<Ticket>ticketList=passenger1.getBookedTickets();
                    ticketList.add(ticket1);
                    passenger1.setBookedTickets(ticketList);
                    passengerRepository.save(passenger1);
                }


                return ticket1.getTicketId();
            }
            else{
                throw new Exception("Less tickets are available");
            }


    }

    private Ticket createTicket(Train train, BookTicketEntryDto bookTicketEntryDto) throws Exception {

        Ticket ticket = new Ticket();
        ticket.setTrain(train);
        ticket.setFromStation(bookTicketEntryDto.getToStation());
        ticket.setToStation(bookTicketEntryDto.getToStation());
        int fare = calculateFare(bookTicketEntryDto.getFromStation(), bookTicketEntryDto
                .getToStation(), train.getRoute());

        ticket.setTotalFare(fare);

        return ticket;
    }

    private int calculateFare(Station fromStation, Station toStation, String route) throws Exception {
        int fare =0;
        if(fromStation!=toStation){
            String[]stations = route.split(",");
            int startIndex = -1;

            int endIndex = -1;

            for(int i=0; i<stations.length; i++) {
                if (stations[i].equals(fromStation.name())) {
                    startIndex = i;
                }
                if (stations[i].equals(toStation.name())) {
                    endIndex = i;
                }
            }

                if(startIndex!=-1 && endIndex!=-1){
                    int start = Math.min(startIndex, endIndex);
                    int end = Math.max(startIndex, endIndex);

                    for(int i=start;i<end;i++){
                        fare+=300;
                }
            }
                else{
                    throw new Exception("Invalid stations");
                }
        }
        return fare;
    }
}
