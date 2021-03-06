require 'rubygems'
require 'sinatra'
require 'dm-core'
require 'dm-validations'
require 'dm-migrations'
require 'sanitize'
require 'nate'

# Datamapper setup
db_temp_file = Tempfile.new( "nate_todo_db" )
DataMapper.setup( :default, "sqlite://#{db_temp_file.path}" )

class ToDo
  include DataMapper::Resource

  property :id,         Serial
  property :title,      String
  property :created_at, DateTime
  property :complete,   Boolean, :default => false

  validates_presence_of :title
end

DataMapper.finalize
DataMapper.auto_migrate!

# templates
Layout  = Nate::Engine.from_file 'templates/layout.html'
List    = Nate::Engine.from_file 'templates/list.html'
AddToDo = Nate::Engine.from_file 'templates/add.html' 

# controllers
get '/' do
  todos = ToDo.all( :complete => false )
  todo_data = todos.collect do |todo|
    { '.title' => todo.title,  'input[@name=id]' => { '@@value' => todo.id }}
  end 
  data = { '.todo' => todo_data }
  
  list = List.inject_with( '.todolist' => data ).select( '###content' )
  Layout.inject_with( { '#content' => list } ).to_xml
end

get '/new' do
  Layout.inject_with( { '#content' => AddToDo.select( '###content' ) } ).to_xml
end

post '/add' do
  ToDo.create( :title => Sanitize.clean( params[:title] ), :created_at => Time.now )
  redirect '/'
end

post '/finished' do
  todo = ToDo.get( params[:id] )
  todo.update( :complete => true )
  redirect '/'
end
