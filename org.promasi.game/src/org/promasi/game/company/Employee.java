package org.promasi.game.company;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.promasi.game.GameException;
import org.promasi.utilities.logger.ILogger;
import org.promasi.utilities.logger.LoggerFactory;
import org.promasi.utilities.serialization.SerializationException;


/**
 * 
 * @author alekstheod
 * Represents an employee.(Developer,Tester,Designer etc). All the fields of the
 * employee(experienced etc) have a range of 0.0-10.0
 */
public class Employee 
{	
	/**
     * The name of the person.
     */
    protected String _firstName;

    /**
     * The last name of the person.
     */
    protected String _lastName;

    /**
     * The employee identifier string.
     */
    protected String _employeeId;
    
	/**
     * The salary of the employee.
     */
    protected double _salary;

    
    /**
     * The CV of the employee.
     */
    protected String _curriculumVitae;
    
    /**
     * List of employee skills,
     * each employee has a different list of skills
     * these skills will be used as the inputs of the
     * simulation system.
     */
    protected Map<String, Double> _employeeSkills;
    
	/**
	 * List of object listeners, needed to
	 * inform other objects about the important
	 * state changes of the current object.
	 */
	private List<IEmployeeListener> _listeners;
    
    /**
     * List of assigned employee tasks. Employee
     * will apply his skills to the executing task.
     */
    protected Map<String ,EmployeeTask> _employeeTasks;
    
    /**
     * The supervisor of employee, in real
     * world a project manager, in the ProMaSi system
     * a player.
     */
    private String _supervisor;
    
    /**
     * 
     */
    private Lock _lockObject;
    
    /**
     * 
     */
    private static final ILogger _logger = LoggerFactory.getInstance(Employee.class);
    
    /**
     * 
     * @param firstName
     * @param lastName
     * @param employeeId
     * @param curriculumVitae
     * @param salary
     * @param employeeSkills
     * @throws GameException
     */
    public Employee(String firstName, String lastName, String employeeId, String curriculumVitae, double salary,Map<String, Double> employeeSkills)throws GameException
   {
    	
    	if(firstName==null){
    		throw new GameException("Wrong argument firstName==null");
    	}
    	
        if(lastName==null){
        	throw new GameException("Wrong argument lastName==null");
        }
        
        if(employeeId==null){
        	throw new GameException("Wrong argument employeeId==null");
        }
        
        if(curriculumVitae==null){
        	throw new GameException("Wrong argument curriculumVitae==null");
        }

        if(employeeSkills==null){
        	throw new GameException("Wrong argument employeeSkills==null");
        }
        
        for(Map.Entry<String, Double> entry : employeeSkills.entrySet()){
        	if(entry.getKey()==null || entry.getValue()==null){
        		throw new IllegalArgumentException("Wrong argument employeeSkills contains null");
        	}
        }
        
        _firstName=firstName;
        _lastName=lastName;
        _employeeId=employeeId;
        _curriculumVitae=curriculumVitae;
        _salary=salary;
        _employeeSkills=employeeSkills;
        _lockObject = new ReentrantLock();
        _employeeTasks=new TreeMap<String, EmployeeTask>();
        _listeners = new LinkedList<IEmployeeListener>();
        _logger.info("Employee initialization succeed :" + _employeeId);
    }

    /**
     * @return The {@link #_salary}.
     */
    public double getSalary ( ){
        return _salary;
    }

    /**
     * @return The {@link #_curriculumVitae}.
     */
    public String getCurriculumVitae ( ){
        return _curriculumVitae;
    }
    
    /**
     * 
     * @return
     */
    public String getFirstName(){
    	return _firstName;
    }
    
    /**
     * 
     * @return
     */
    public String getLastName(){
    	return _lastName;
    }
    
    /**
     * 
     * @return
     */
    public String getEmployeeId(){
    	return _employeeId;
    }
    
    /**
     * 
     * @return
     */
    public String getSupervisor(){
    	try{
    		_lockObject.lock();
    		return _supervisor;
    	}finally{
    		_lockObject.unlock();
    	}
    }
    
    /**
     * 
     * @param supervisor
     */
    public void setSupervisor( String supervisor ){
    	try{
    		_lockObject.lock();
    		_supervisor = supervisor;
    	}finally{
    		_lockObject.unlock();
    	}
    }
    
	/**
	 * 
	 * @return
	 * @throws SerializationException
	 */
	public EmployeeMemento getMemento(){
		return new EmployeeMemento(this);
	}

    /**
     * 
     * @param employeeTask
     * @return
     * @throws SerializationException 
     */
    public boolean assignTasks(List<EmployeeTask> employeeTasks){
    	boolean result = false;
    	
    	try{
    		_lockObject.lock();
        	if(employeeTasks!=null){
        		result = true;
        		
            	for(EmployeeTask task : employeeTasks){
            		if( _employeeTasks.containsKey(task.getTaskName())){
            			_logger.warn("Task assign failed because a task with the same Id is already assigned :" + task.getTaskName());
            			result = false;
            			break;
            		}
            		
                	for(Map.Entry<String , EmployeeTask> entry: _employeeTasks.entrySet()){
                		if( entry.getValue().conflictsWithTask(task) ){
                			result = false;
                			_logger.warn("Task assign failed because a conflict with a task :" + entry.getKey());
                			break;
                		}
                	}
            	}
            	
        		List<EmployeeTask> tasks = new LinkedList<EmployeeTask>(employeeTasks);
        		for( EmployeeTask task : employeeTasks ){
        			for( EmployeeTask newTask : tasks){
        				if( task != newTask && task.conflictsWithTask(newTask)){
        					_logger.warn("Task assign failed because a conflict with a task :" + newTask.getTaskName());
        					result = false;
        				}
        			}
        		}
            	
            	if( result ){

                	for(EmployeeTask task : employeeTasks){
                		_employeeTasks.put(task.getTaskName(), task);
                	}
                	
                	for( IEmployeeListener listener : _listeners ){
                		listener.taskAssigned(_supervisor, getMemento());
                	}
            		
            	}else{
                	for( IEmployeeListener listener : _listeners ){
                		listener.tasksAssignFailed(_supervisor, getMemento());
                	}
            	}
        	}
    	}finally{
    		_lockObject.unlock();
    	}
	
		return result;
    }
    
    /**
     * 
     * @return
     */
    public boolean removeAllTasks(){
    	boolean result = false;
    	try{
    		_lockObject.lock();
        	for(Map.Entry<String , EmployeeTask> entry : _employeeTasks.entrySet()){
        		result = true;
    			for ( IEmployeeListener listener : _listeners ){
    				listener.taskDetached(_supervisor, getMemento(), entry.getValue().getMemento());
    			}
        	}
        	
        	_employeeTasks.clear();
        	result = true;
    	}finally{
    		_lockObject.unlock();
    	}

    	return result;
    }
    
    /**
     * 
     * @param employeeTask
     * @return true in case of remove task succeed, false otherwise.
     */
    public boolean removeEmployeeTask(EmployeeTask employeeTask){
    	boolean result = false;
    	try{
    		_lockObject.lock();
        	if(employeeTask!=null){
            	if(_employeeTasks.containsKey(employeeTask.getFirstStep() ) ){
            		EmployeeTask task=_employeeTasks.get(employeeTask.getFirstStep() );
            		if(task==employeeTask){
            			_employeeTasks.remove(task.getFirstStep());
            			result = true;
            			for ( IEmployeeListener listener : _listeners ){
            				listener.taskDetached(_supervisor, getMemento(), task.getMemento());
            			}
            		}
            	}
        	}
    	}finally{
    		_lockObject.unlock();
    	}
    	
    	return result;
    }
    
    /**
     * 
     * @return
     */
    public boolean  executeTasks(int currentStep){
    	boolean result = false;
    	
    	try{
    		_lockObject.lock();
        	if(!_employeeTasks.isEmpty()){
            	Map<String, EmployeeTask> employeeTasks=new TreeMap<String, EmployeeTask> ();
            	for(Map.Entry<String ,EmployeeTask> entry: _employeeTasks.entrySet()){
        			entry.getValue().executeTask(_employeeSkills, currentStep);
        			if(entry.getValue().isValid(currentStep)){
        				employeeTasks.put(entry.getKey(), entry.getValue());
        			}
        			
        			result = true;
            	}
            	
            	_employeeTasks=employeeTasks;
        	}
        	
    	}finally{
    		_lockObject.unlock();
    	}

    	return result;
    }

    /**
     * 
     * @param listener
     * @return
     */
    public boolean addListener(IEmployeeListener listener){
    	boolean result = false;
    	
    	try{
    		_lockObject.lock();
    		if( !_listeners.contains(listener) ){
    			result = _listeners.add(listener);
    		}
    	}finally{
    		_lockObject.unlock();
    	}
    	
    	return result;
    }
    

    /**
     * 
     * @param listener
     * @return
     */
    public boolean removeListener(IEmployeeListener listener){
    	boolean result = false;
    	try{
    		_lockObject.lock();
    		if( _listeners.contains(listener) ){
    			result = _listeners.remove(listener);
    		}
    	}finally{
    		_lockObject.unlock();
    	}
    	
    	return result;
    }
    
    /**
     * Will return the list of assigned tasks.
     * @return instance of {@link=Map<String, EmployeeTask>}
     */
    public Map<String, EmployeeTask> getAssignedTasks(){
    	return new TreeMap<>(_employeeTasks);
    }
    
    /**
     * 
     */
    public void removeListeners(){
    	try{
    		_lockObject.lock();
    		_listeners.clear();
    	}finally{
    		_lockObject.unlock();
    	}
    }
}
