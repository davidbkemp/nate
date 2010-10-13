require 'rubygems'
require 'sinatra'
require 'dm-core'
require 'dm-validations'
require 'dm-migrations'
require 'nate'

# Datamapper setup

DataMapper.setup( :default, "sqlite::memory:")

class ToDo
  include DataMapper::Resource

  property :id,         Serial
  property :title,      String
  property :created_at, DateTime
  property :complete,   Boolean, :default=>false

  validates_presence_of :title
end

DataMapper.finalize
DataMapper.auto_migrate!

# helpers

def todo_list
  template = Nate::Engine.new( (File.new 'list.html').read )
  template.inject_with( { '.todo' => ToDo.all( :complete => false ).collect { |todo| todo.title } } )
end

# controllers

get '/' do
  todo_list()
end

get '/new' do
  File.new 'form.html'
end

post '/add' do
  ToDo.create( :title => params[:title], :created_at => Time.now )
  redirect '/'
end